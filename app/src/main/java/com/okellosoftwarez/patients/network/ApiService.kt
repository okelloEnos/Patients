package com.okellosoftwarez.patients.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * ApiService
 *
 * Defines all REST API endpoints for the Patient app.
 * Uses Retrofit annotations to map HTTP methods and paths.
 *
 * Base URL: https://patientvisitapis.intellisoftkenya.com/api/
 */
interface ApiService {

    // 🔹 Authentication
    @POST("user/signup")
    suspend fun signup(@Body body: Map<String, Any>): Response<Map<String, Any>>

    @POST("user/login")
    suspend fun login(@Body body: Map<String, Any>): Response<Map<String, Any>>

    // 🔹 Patients
    @POST("patients/register")
    suspend fun registerPatient(@Body body: Map<String, Any>): Response<Map<String, Any>>

    @GET("patients/list")
    suspend fun listPatients(): Response<List<Map<String, Any>>>

    @GET("patients/show/{id}")
    suspend fun showPatient(@Path("id") id: String): Response<Map<String, Any>>

    // 🔹 Vitals
    @POST("vitals/add")
    suspend fun addVitals(@Body body: Map<String, Any>): Response<Map<String, Any>>

    // 🔹 Visits (General & Overweight Assessments)
    @POST("visits/view")
    suspend fun visitsView(@Body body: Map<String, Any>): Response<List<Map<String, Any>>>

    @GET("visits/add")
    suspend fun visitsAdd(): Response<Map<String, Any>>
}
