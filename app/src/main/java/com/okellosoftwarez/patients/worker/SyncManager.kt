package com.okellosoftwarez.patients.worker

import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncManager
 *
 * Central helper for enqueuing/cancelling WorkManager jobs related to background sync.
 *
 * Usage:
 *   syncManager.enqueueOneTimeSync()
 *   syncManager.enqueuePeriodicSync()
 */
@Singleton
class SyncManager @Inject constructor(
    private val workManager: WorkManager
) {

    companion object {
        /** Unique work name used to keep a single sync pipeline. */
        const val UNIQUE_SYNC_WORK_NAME = "patient_offline_sync_work"

        /** Default periodic sync interval (minimum allowed by WorkManager is 15 minutes). */
        const val DEFAULT_PERIODIC_MINUTES = 15L
    }

    /**
     * Enqueue a one-off sync worker.
     * If one already exists, KEEP prevents duplicate work from being queued.
     */
    fun enqueueOneTimeSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTime = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS, // ✅ correct constant
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            oneTime
        )
    }

    /**
     * Enqueue (or keep) a periodic sync worker that runs every [intervalMinutes].
     */
    fun enqueuePeriodicSync(intervalMinutes: Long = DEFAULT_PERIODIC_MINUTES) {
        val interval = if (intervalMinutes < DEFAULT_PERIODIC_MINUTES) DEFAULT_PERIODIC_MINUTES else intervalMinutes

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(interval, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS, // ✅ correct constant
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    /** Cancel the unique sync work (both periodic & one-off). */
    fun cancelSync() {
        workManager.cancelUniqueWork(UNIQUE_SYNC_WORK_NAME)
    }
}
