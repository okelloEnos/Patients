package com.okellosoftwarez.patients.network

import android.content.Context
import android.util.Log
import com.okellosoftwarez.patients.util.PrefsHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthInterceptor
 *
 * Automatically attaches an Authorization header to every request
 * if a token is available in local preferences.
 *
 * Example:
 *   Authorization: Bearer <token>
 */
@Singleton
class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = PrefsHelper(context).getAuthToken()

        return try {
            if (!token.isNullOrBlank()) {
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error adding Authorization header", e)
            chain.proceed(originalRequest)
        }
    }
}
