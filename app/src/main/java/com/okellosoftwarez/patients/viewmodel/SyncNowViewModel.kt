package com.okellosoftwarez.patients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okellosoftwarez.patients.repository.PatientRepository
import com.okellosoftwarez.patients.worker.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SyncNowViewModel @Inject constructor(
    private val repo: PatientRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount = _pendingCount.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    init {
        refreshPendingCount()
    }

    /** Refresh the pending count from DB. */
    fun refreshPendingCount() {
        viewModelScope.launch {
            try {
                val count = repo.getPendingCount()
                _pendingCount.value = count
            } catch (ex: Exception) {
                _message.value = "Failed to read pending count: ${ex.message}"
            }
        }
    }

    /**
     * Trigger an immediate sync:
     *  - enqueue a one-off sync worker
     *  - poll pending count until cleared or timeout
     *
     * Note: WorkManager executes the actual sync; this method polls the DB to
     * observe whether worker completed the queued items.
     */
    fun syncNow(timeoutSeconds: Long = 30) {
        // If already syncing, ignore
        if (_isSyncing.value) return

        viewModelScope.launch {
            _isSyncing.value = true
            _message.value = null

            try {
                // Enqueue work
                syncManager.enqueueOneTimeSync()

                // Poll pending count until zero or timeout
                val deadline = timeoutSeconds
                var elapsed = 0L
                val pollIntervalMs = 1000L

                while (elapsed < deadline) {
                    val count = repo.getPendingCount()
                    _pendingCount.value = count
                    if (count == 0) {
                        _message.value = "Sync completed"
                        _isSyncing.value = false
                        return@launch
                    }
                    delay(pollIntervalMs)
                    elapsed += 1L
                }

                // Timeout reached — still may be syncing in background
                _message.value = "Sync scheduled. Pending items: ${_pendingCount.value}"
            } catch (ex: Exception) {
                _message.value = "Sync failed: ${ex.message}"
            } finally {
                _isSyncing.value = false
                // refresh count one final time (in case it changed)
                refreshPendingCount()
            }
        }
    }

    /** Clear ephemeral UI message (e.g., after snackbar shown). */
    fun clearMessage() {
        _message.value = null
    }
}
