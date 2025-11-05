package com.okellosoftwarez.patients.network

import android.util.Log
import com.okellosoftwarez.patients.util.PrefsHelper
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthInterceptor"

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefsHelper: PrefsHelper
) : Interceptor {

    // Simple in-memory cache to avoid reading SharedPreferences on every request.
    // If you update token elsewhere, call clearCache() or save via PrefsHelper which should update it.
    @Volatile
    private var cachedToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // If the request already contains Authorization header, don't override it.
        if (original.header("Authorization") != null) {
            return try {
                chain.proceed(original)
            } catch (ex: Exception) {
                Log.e(TAG, "Network request failed (existing Authorization): ${ex.message}", ex)
                throw ex
            }
        }

        // Load token from cache or PrefsHelper
        val token = cachedToken ?: prefsHelper.getAuthToken()?.also { cachedToken = it }

        return try {
            if (!token.isNullOrBlank()) {
                val req = original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(req)
            } else {
                chain.proceed(original)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error adding Authorization header or executing request: ${ex.message}", ex)
            throw ex
        }
    }

    /**
     * Clears the in-memory token cache. Call this when you explicitly change the stored token
     * (for example after login/logout) so the interceptor reads the new token on next request.
     */
    fun clearCache() {
        cachedToken = null
    }
}
