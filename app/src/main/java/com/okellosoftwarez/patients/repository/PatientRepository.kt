package com.okellosoftwarez.patients.repository

//import com.example.patientapp.data.AppDatabase
import com.okellosoftwarez.patients.data.models.*
//import com.okellosoftwarez.patientsp.network.ApiService
import com.google.gson.Gson
import com.okellosoftwarez.patients.data.AppDatabase
import com.okellosoftwarez.patients.data.models.Patient
import com.okellosoftwarez.patients.data.models.Vitals
import com.okellosoftwarez.patients.network.ApiService
import com.okellosoftwarez.patients.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PatientRepository
 *
 * Handles all data operations:
 * - Local database CRUD via Room
 * - Queuing data for remote sync
 * - Simple offline-first logic
 *
 * Injected with Hilt dependencies for AppDatabase, ApiService, and Gson.
 */
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

    /** Inserts a patient locally */
    suspend fun insertPatient(patient: Patient): Long = patientDao.insert(patient)

    /** Counts how many patients share a specific Patient ID (to enforce uniqueness) */
    suspend fun countByPatientId(pid: String): Int = patientDao.countByPatientId(pid)

    /** Returns a LiveData list of all patients */
    fun getAllPatients() = patientDao.getAllPatients()

    /** Returns a specific patient by local database ID */
    suspend fun getPatientById(id: Long) = patientDao.getPatientById(id)

    /**
     * Saves a Vitals record locally and queues it for syncing.
     * BMI status determines which assessment screen follows.
     */
    suspend fun saveVitals(vitals: Vitals): Long = withContext(Dispatchers.IO) {
        val id = vitalsDao.insert(vitals)

        val patient = getPatientById(vitals.patientOwnerId)
        val payload = mapOf(
            "patient_id" to patient?.patientId,
            "visit_date" to DateUtils.toIsoDate(vitals.visitDateEpoch),
            "height_cm" to vitals.heightCm,
            "weight_kg" to vitals.weightKg,
            "bmi" to vitals.bmi
        )

        // Queue payload for background sync
        pendingDao.insert(
            PendingSync(
                endpoint = "vitals/add",
                payloadJson = gson.toJson(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        id
    }

    /**
     * Saves a General Assessment (for BMI < 25) and queues for sync.
     */
    suspend fun saveGeneralAssessment(a: GeneralAssessment): Long = withContext(Dispatchers.IO) {
        val id = generalDao.insert(a)
        val patient = getPatientById(a.patientOwnerId)

        val payload = mapOf(
            "patient_id" to patient?.patientId,
            "visit_date" to DateUtils.toIsoDate(a.visitDateEpoch),
            "general_health" to a.generalHealth,
            "ever_on_diet" to a.everOnDiet,
            "comments" to a.comments
        )

        pendingDao.insert(
            PendingSync(
                endpoint = "visits/add",
                payloadJson = gson.toJson(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        id
    }

    /**
     * Saves an Overweight Assessment (for BMI >= 25) and queues for sync.
     */
    suspend fun saveOverweightAssessment(a: OverweightAssessment): Long = withContext(Dispatchers.IO) {
        val id = overweightDao.insert(a)
        val patient = getPatientById(a.patientOwnerId)

        val payload = mapOf(
            "patient_id" to patient?.patientId,
            "visit_date" to DateUtils.toIsoDate(a.visitDateEpoch),
            "general_health" to a.generalHealth,
            "using_drugs" to a.usingDrugs,
            "comments" to a.comments
        )

        pendingDao.insert(
            PendingSync(
                endpoint = "visits/add",
                payloadJson = gson.toJson(payload),
                createdAt = System.currentTimeMillis()
            )
        )

        id
    }

    /** Gets the latest vitals record for a given patient */
    suspend fun getLatestVitals(patientId: Long) = vitalsDao.getLatestForPatient(patientId)

    /** Retrieves all queued PendingSync records */
    suspend fun getPendingSyncs() = pendingDao.getAll()

    /** Deletes a pending sync after successful submission */
    suspend fun deletePending(p: PendingSync) = pendingDao.delete(p)

    /** Updates a pending sync with retry count or error details */
    suspend fun updatePending(p: PendingSync) = pendingDao.update(p)
}
