package com.okellosoftwarez.patients

import android.content.Context
import com.okellosoftwarez.patients.data.AppDatabase
import com.google.gson.Gson
import com.okellosoftwarez.patients.network.ApiService
import com.okellosoftwarez.patients.network.AuthInterceptor
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

/**
 * AppModule
 *
 * Centralized dependency provider for:
 *  - Room database
 *  - Retrofit + OkHttp with AuthInterceptor
 *  - Gson serialization
 *  - ApiService interface
 */
private const val BASE_URL = "https://patientvisitapis.intellisoftkenya.com/api/"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Provides Room Database instance */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    /** Provides Gson instance */
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    /** Provides AuthInterceptor for token injection */
    @Provides
    @Singleton
    fun provideAuthInterceptor(@ApplicationContext context: Context): AuthInterceptor {
        return AuthInterceptor(context)
    }

    /** Provides HttpLoggingInterceptor for debugging */
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    /** Provides OkHttp client with AuthInterceptor + Logging */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /** Provides Retrofit instance configured with Gson and OkHttp */
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /** Provides ApiService interface implementation */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

