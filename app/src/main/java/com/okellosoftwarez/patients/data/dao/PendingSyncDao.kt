package com.okellosoftwarez.patients.data.dao

import androidx.room.*
import com.okellosoftwarez.patients.data.models.PendingSync

/**
 * PendingSyncDao
 *
 * DAO for managing the offline sync queue.
 * Worker (WorkManager) should:
 *  - query all pending items (getAll)
 *  - attempt request -> on success delete(p)
 *  - on failure update(p.copy(attemptCount = ..., lastError = "..."))
 */
@Dao
interface PendingSyncDao {

    /** Insert a new pending sync (queue an API payload). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pending: PendingSync): Long

    /** Get all pending syncs ordered oldest-first (preserve FIFO). */
    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingSync>

    /** Delete a pending sync after success. */
    @Delete
    suspend fun delete(pending: PendingSync)

    /** Update a pending sync (increment attempts, attach lastError, etc). */
    @Update
    suspend fun update(pending: PendingSync)

    /** Optional convenience: delete all successful/old items (cleanup). */
    @Query("DELETE FROM pending_sync WHERE attemptCount >= :maxAttempts OR createdAt < :olderThan")
    suspend fun cleanup(maxAttempts: Int = 5, olderThan: Long = 0L)
}
