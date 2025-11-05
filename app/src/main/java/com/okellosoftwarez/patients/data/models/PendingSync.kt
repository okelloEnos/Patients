package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PendingSync
 *
 * Represents a queued network operation that must be retried later (offline-first).
 * - endpoint: API path (e.g., "patients/register", "vitals/add", "visits/add")
 * - payloadJson: serialized request body (Gson)
 * - createdAt: epoch millis when queued
 * - attemptCount: number of sync attempts
 * - lastError: last error message (optional) to display/debug
 */
@Entity(tableName = "pending_sync")
data class PendingSync(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val endpoint: String,
    val payloadJson: String,
    val createdAt: Long,
    val attemptCount: Int = 0,
    val lastError: String? = null
)
