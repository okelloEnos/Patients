package com.okellosoftwarez.patients.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.okellosoftwarez.patients.data.AppDatabase
import com.okellosoftwarez.patients.data.dao.PatientWithLastVitals
import com.okellosoftwarez.patients.data.models.*
import com.okellosoftwarez.patients.network.*
import com.okellosoftwarez.patients.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PatientRepository"

@Singleton
class PatientRepository @Inject constructor(
    private val db: AppDatabase,
    private val api: ApiService,
    private val gson: Gson
) {
    private val patientDao = db.patientDao()
    private val vitalsDao = db.vitalsDao()
    private val generalDao = db.generalAssessmentDao()
    private val overweightDao = db.overweightAssessmentDao()
    private val pendingDao = db.pendingSyncDao()

    // ------------------ Read helpers ---------------------

    fun getAllPatients(): LiveData<List<com.okellosoftwarez.patients.data.models.Patient>> =
        patientDao.getAllPatients()

    fun getPatientsWithLastVitals(): LiveData<List<PatientWithLastVitals>> =
        patientDao.getPatientsWithLastVitals()

    fun getPatientsByVisitDate(visitDateEpoch: Long): LiveData<List<PatientWithLastVitals>> =
        patientDao.getPatientsByVisitDate(visitDateEpoch)

    suspend fun getPatientById(id: Long): com.okellosoftwarez.patients.data.models.Patient? =
        withContext(Dispatchers.IO) { patientDao.getPatientById(id) }

    suspend fun getPendingCount(): Int = withContext(Dispatchers.IO) { pendingDao.getAll().size }

    suspend fun getPendingSyncs(): List<PendingSync> = withContext(Dispatchers.IO) { pendingDao.getAll() }

    // ------------------ Utility / DAO helpers ---------------------

    suspend fun existsVitalsForPatientOnDate(patientId: Long, visitDateEpoch: Long): Int =
        withContext(Dispatchers.IO) { vitalsDao.existsForPatientOnDate(patientId, visitDateEpoch) }

    suspend fun countByPatientId(pid: String): Int = withContext(Dispatchers.IO) {
        patientDao.countByPatientId(pid)
    }

    // ------------------ Insert / Save with immediate sync ---------------------

    /**
     * Insert patient locally then attempt immediate remote registration.
     * On remote failure a PendingSync is queued.
     */
    suspend fun insertPatient(patient: com.okellosoftwarez.patients.data.models.Patient): Long =
        withContext(Dispatchers.IO) {
            val localId = patientDao.insert(patient)

            // Build request DTO using backend field names you provided in Postman
            val req = RegisterPatientRequest(
                firstname = patient.firstName,
                lastname = patient.lastName,
                unique = patient.patientId,
                dob = patient.dobEpoch?.let { DateUtils.toYMD(it) } ?: "", // ensure "YYYY-MM-DD"
                gender = patient.gender ?: "",
                regDate = DateUtils.toYMD(patient.registrationDateEpoch)
            )

            try {
                val resp = safeApiCall { api.registerPatient(req) }
                if (isSuccessfulAndOk(resp)) {
                    Log.d(TAG, "Remote patient register success for localId=$localId")
                } else {
                    Log.w(TAG, "Remote patient register failed -> queue pending (code=${resp?.code()})")
                    queuePending("patients/register", req)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Exception registering patient remotely, queued", ex)
                queuePending("patients/register", req, ex.message)
            }

            localId
        }

    /**
     * Save vitals locally and attempt immediate upload to API. If network fails, queue pending.
     */
    suspend fun saveVitals(vitals: Vitals): Long = withContext(Dispatchers.IO) {
        val id = vitalsDao.insert(vitals)

        // Prepare VitalsRequest matching your Postman shape (strings for numeric fields)
        val patient = patientDao.getPatientById(vitals.patientOwnerId)
        val req = VitalsRequest(
            visitDate = DateUtils.toYMD(vitals.visitDateEpoch),
            height = vitals.heightCm.toString(),
            weight = vitals.weightKg.toString(),
            bmi = vitals.bmi.toString(),
            patientId = patient?.patientId ?: ""
        )
//id=5, ownerId=2, epoch=1760043600000, h=155.0, w=70, bmi=29.1, remoteId=NULL
        try {
            val resp = safeApiCall { api.addVital(req) }
            if (isSuccessfulAndOk(resp)) {
                ///
                val remoteId = resp?.body()?.data?.id
                // Find the local vitals entry by unique keys (e.g. patientOwnerId + visitDateEpoch)
//                val patientId = patient?.patientId?.toLong() ?: 0L
//                val visitDate = vitals.visitDateEpoch.toString()
//                val localVitals = db.vitalsDao().findByPatientAndDate(patientId = patientId, DateUtils.fromIsoDate(visitDate) ?: 0L)
                val localVitals = db.vitalsDao().getVitalById(id)
                if (localVitals != null && remoteId != null) {
                    db.vitalsDao().update(localVitals.copy(remoteId = remoteId.toLong()))
                }
                ///
//
                Log.d(TAG, "Vitals synced remotely (local vitals id=$id)")
            } else {
                Log.w(TAG, "Vitals remote failed; queuing pending (code=${resp?.code()})")
                queuePending("vital/add", req)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception uploading vitals, queued", ex)
            queuePending("vital/add", req, ex.message)
        }

        id
    }

    /**
     * Save GeneralAssessment (BMI < 25) and attempt immediate upload (queue on failure).
     * Uses visits/add endpoint with the "on_diet" field set.
     */
    suspend fun saveGeneralAssessment(a: GeneralAssessment): Long = withContext(Dispatchers.IO) {
        val id = generalDao.insert(a)

        val patient = patientDao.getPatientById(a.patientOwnerId)
        val patientId = patient?.id ?: 0L
        val visitDate = a.visitDateEpoch
        val localVitals = db.vitalsDao().findByPatientAndDate(patientId = patientId, visitDateEpoch = visitDate)
        val vitalId = localVitals?.remoteId

        val req = VisitAddRequest(
            generalHealth = a.generalHealth,
            onDiet = if (a.everOnDiet) "Yes" else "No",
            onDrugs = "No",
            comments = a.comments,
            visitDate = DateUtils.toYMD(a.visitDateEpoch),
            patientId = patient?.patientId ?: "",
            vitalId = (vitalId ?: "").toString()
        )

        try {
            val resp = safeApiCall { api.addVisit(req) }
            if (isSuccessfulAndOk(resp)) {
                Log.d(TAG, "General assessment remote success (localId=$id)")
            } else {
                Log.w(TAG, "General assessment remote failed; queueing (code=${resp?.code()})")
                queuePending("visits/add", req)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception uploading general assessment, queued", ex)
            queuePending("visits/add", req, ex.message)
        }

        id
    }

    /**
     * Save OverweightAssessment (BMI >= 25) and attempt immediate upload (queue on failure).
     */
    suspend fun saveOverweightAssessment(a: OverweightAssessment): Long = withContext(Dispatchers.IO) {
        val id = overweightDao.insert(a)

        val patient = patientDao.getPatientById(a.patientOwnerId)
        val patientId = patient?.id ?: 0L
        val visitDate = a.visitDateEpoch
        val localVitals = db.vitalsDao().findByPatientAndDate(patientId = patientId, visitDateEpoch = visitDate)
val vitalId = localVitals?.remoteId
//        if (localVitals != null && remoteId != null) {
//            db.vitalsDao().update(localVitals.copy(remoteId = remoteId.toLong()))
//        }

        val req = VisitAddRequest(
            generalHealth = a.generalHealth,
            onDiet = "No",
            onDrugs = if (a.usingDrugs) "Yes" else "No",
            comments = a.comments,
            visitDate = DateUtils.toYMD(a.visitDateEpoch),
            patientId = patient?.patientId ?: "",
            vitalId = (vitalId ?: "").toString() // supply if you have a vitals row id from earlier step
        )

        try {
            val resp = safeApiCall { api.addVisit(req) }
            if (isSuccessfulAndOk(resp)) {
                Log.d(TAG, "Overweight assessment remote success (localId=$id)")
            } else {
                Log.w(TAG, "Overweight assessment remote failed; queueing (code=${resp?.code()})")
                queuePending("visits/add", req)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Exception uploading overweight assessment, queued", ex)
            queuePending("visits/add", req, ex.message)
        }

        id
    }

    /** Delete / update pending (used by worker) */
    suspend fun deletePending(p: PendingSync) = withContext(Dispatchers.IO) { pendingDao.delete(p) }
    suspend fun updatePending(p: PendingSync) = withContext(Dispatchers.IO) { pendingDao.update(p) }

    // ------------------ helpers ---------------------

    private suspend fun queuePending(endpoint: String, payloadObj: Any, lastError: String? = null) {
        withContext(Dispatchers.IO) {
            try {
                val json = gson.toJson(payloadObj)
                pendingDao.insert(
                    PendingSync(
                        endpoint = endpoint,
                        payloadJson = json,
                        createdAt = System.currentTimeMillis(),
                        lastError = lastError
                    )
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to insert pending sync record", ex)
            }
        }
    }

    private fun <T> isSuccessfulAndOk(resp: Response<GenericApiResponse<T>>?): Boolean {
        if (resp == null) return false
        if (!resp.isSuccessful) return false
        val body = resp.body() ?: return false
        return body.success
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Response<T>? {
        return try {
            call()
        } catch (io: IOException) {
            Log.w(TAG, "Network I/O error", io)
            null
        } catch (ex: Exception) {
            Log.e(TAG, "Unexpected API error", ex)
            null
        }
    }
}

