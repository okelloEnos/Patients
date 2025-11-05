package com.okellosoftwarez.patients

import android.content.Context
import androidx.work.WorkManager
import com.google.gson.Gson
import com.okellosoftwarez.patients.data.AppDatabase
import com.okellosoftwarez.patients.network.ApiService
import com.okellosoftwarez.patients.network.AuthInterceptor
import com.okellosoftwarez.patients.util.PrefsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "https://patientvisitapis.intellisoftkenya.com/api/"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // -------------------------
    // Database
    // -------------------------
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    // -------------------------
    // Gson
    // -------------------------
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    // -------------------------
    // PrefsHelper
    // -------------------------
    /**
     * Provide the PrefsHelper used across the app (token storage, last-sync time, etc).
     * Ensure your PrefsHelper has an @Inject constructor or that this provider matches its constructor.
     */
    @Provides
    @Singleton
    fun providePrefsHelper(@ApplicationContext context: Context): PrefsHelper =
        PrefsHelper(context)

    // -------------------------
    // Interceptors & OkHttp
    // -------------------------
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideAuthInterceptor(prefsHelper: PrefsHelper): AuthInterceptor =
        AuthInterceptor(prefsHelper)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    // -------------------------
    // Retrofit & ApiService
    // -------------------------
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // -------------------------
    // WorkManager (convenience)
    // -------------------------
    /**
     * Provide WorkManager instance for enqueuing sync jobs from the repository or UI.
     * Note: ensure your Application class is configured for Hilt and WorkManager Hilt integration
     * if you want to use @HiltWorker in your workers.
     */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
