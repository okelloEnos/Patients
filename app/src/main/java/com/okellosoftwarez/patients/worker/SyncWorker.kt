package com.okellosoftwarez.patients.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.okellosoftwarez.patients.data.AppDatabase
import com.okellosoftwarez.patients.data.models.PendingSync
import com.okellosoftwarez.patients.network.ApiService
import com.okellosoftwarez.patients.network.RegisterPatientRequest
import com.okellosoftwarez.patients.network.VitalsRequest
import com.okellosoftwarez.patients.network.VisitAddRequest
import com.okellosoftwarez.patients.network.GenericApiResponse
import com.okellosoftwarez.patients.network.RegisterPatientResponseData
import com.okellosoftwarez.patients.network.VitalsAddResponseData
import com.okellosoftwarez.patients.network.VisitAddResponseData
import com.okellosoftwarez.patients.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: ApiService,
    private val db: AppDatabase,
    private val gson: Gson
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    private val pendingDao = db.pendingSyncDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val pendingList = pendingDao.getAll()
            if (pendingList.isEmpty()) return@withContext Result.success()

            for (p in pendingList) {
                try {
                    val ok = replayPending(p)
                    if (ok) {
                        pendingDao.delete(p)
                        Log.d(TAG, "Synced and removed pending id=${p.id}")
                    } else {
                        pendingDao.update(p.copy(attemptCount = p.attemptCount + 1, lastError = "remote rejected"))
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Exception while syncing pending id=${p.id}", ex)
                    pendingDao.update(p.copy(attemptCount = p.attemptCount + 1, lastError = ex.message))
                }
            }
            return@withContext Result.success()
        } catch (ex: Exception) {
            Log.e(TAG, "SyncWorker fatal", ex)
            return@withContext Result.retry()
        }
    }

    private suspend fun replayPending(p: PendingSync): Boolean = withContext(Dispatchers.IO) {
        val endpoint = p.endpoint ?: return@withContext false
        val payloadJson = p.payloadJson ?: return@withContext false

        return@withContext when (endpoint.trimEnd('/')) {
            "patients/register" -> {
                val req = gson.fromJson(payloadJson, RegisterPatientRequest::class.java)
                val resp: Response<GenericApiResponse<RegisterPatientResponseData>>? = safeApiCall { api.registerPatient(req) }
                isSuccessfulAndOk(resp)
            }
            "vital/add" -> {
                val req = gson.fromJson(payloadJson, VitalsRequest::class.java)
                val resp: Response<GenericApiResponse<VitalsAddResponseData>>? = safeApiCall { api.addVital(req) }
                if (isSuccessfulAndOk(resp)) {
                    val remoteId = resp?.body()?.data?.id
                    // Find the local vitals entry by unique keys (e.g. patientOwnerId + visitDateEpoch)
                    val patientId = "1".toLong()
                    val visitDate = "1"
                    val localVitals = db.vitalsDao().findByPatientAndDate(patientId = patientId, DateUtils.fromIsoDate(visitDate) ?: 0L)
                    if (localVitals != null && remoteId != null) {
                        db.vitalsDao().update(localVitals.copy(remoteId = remoteId.toLong()))
                    }
                }

                isSuccessfulAndOk(resp)
            }
            "visits/add" -> {
                val req = gson.fromJson(payloadJson, VisitAddRequest::class.java)
                val resp: Response<GenericApiResponse<VisitAddResponseData>>? = safeApiCall { api.addVisit(req) }
                isSuccessfulAndOk(resp)
            }
            else -> {
                Log.w(TAG, "Unknown endpoint: $endpoint")
                false
            }
        }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Response<T>? {
        return try {
            call()
        } catch (ex: Exception) {
            Log.w(TAG, "Network error", ex)
            null
        }
    }

    private fun <T> isSuccessfulAndOk(resp: Response<GenericApiResponse<T>>?): Boolean {
        if (resp == null) return false
        if (!resp.isSuccessful) return false
        val body = resp.body() ?: return false
        return body.success
    }
}
