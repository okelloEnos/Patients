package com.okellosoftwarez.patients.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * PrefsHelper handles small key–value persistence for authentication tokens,
 * sync timestamps, and app preferences.
 *
 * Initially it stores a static token (for testing), but can be replaced
 * later by a real login token when auth is implemented.
 */
@Singleton
class PrefsHelper @Inject constructor(
    @ApplicationContext context: Context
) {

    companion object {
        private const val PREF_NAME = "patient_app_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * For testing, we can initialize with a static token.
     * Replace this value with your static Bearer token from backend.
     */
    private val defaultStaticToken =
        "374|tMzJhCupfphpfUe4G2NNc48YXTm7Hn1TNHwdt6PF"

    // region === TOKEN MANAGEMENT ===

    fun saveAuthToken(token: String) {
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun getAuthToken(): String? {
        // fallback to defaultStaticToken if none saved
        return prefs.getString(KEY_AUTH_TOKEN, defaultStaticToken)
    }

    fun clearAuthToken() {
        prefs.edit { remove(KEY_AUTH_TOKEN) }
    }

    // endregion

    // region === SYNC METADATA ===

    fun saveLastSyncTime(timestamp: Long) {
        prefs.edit { putLong(KEY_LAST_SYNC_TIME, timestamp) }
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    // endregion
}
