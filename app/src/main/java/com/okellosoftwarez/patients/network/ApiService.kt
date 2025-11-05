package com.okellosoftwarez.patients.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.Serializable

/**
 * DTOs & ApiService matching the Postman samples you provided.
 *
 * Endpoints used:
 *  - POST {base_url}patients/register
 *  - POST {base_url}vital/add
 *  - POST {base_url}visits/add
 *
 * Field names and response shapes are modelled from your samples.
 */

/* -------------------- Request DTOs -------------------- */

data class RegisterPatientRequest(
    @SerializedName("firstname") val firstname: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("unique") val unique: String,
    @SerializedName("dob") val dob: String,          // "YYYY-MM-DD"
    @SerializedName("gender") val gender: String,
    @SerializedName("reg_date") val regDate: String // "YYYY-MM-DD"
) : Serializable

data class VitalsRequest(
    @SerializedName("visit_date") val visitDate: String, // "YYYY-MM-DD"
    @SerializedName("height") val height: String,       // API example used strings
    @SerializedName("weight") val weight: String,
    @SerializedName("bmi") val bmi: String,
    @SerializedName("patient_id") val patientId: String
) : Serializable

data class VisitAddRequest(
    @SerializedName("general_health") val generalHealth: String,
    @SerializedName("on_diet") val onDiet: String,     // "Yes"/"No" in example
    @SerializedName("on_drugs") val onDrugs: String,   // "Yes"/"No"
    @SerializedName("comments") val comments: String,
    @SerializedName("visit_date") val visitDate: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("vital_id") val vitalId: String
) : Serializable

/* -------------------- Response DTOs -------------------- */

/**
 * Standard wrapper used in your samples:
 * {
 *   "message":"success",
 *   "success": true,
 *   "code": 200,
 *   "data": { ... }
 * }
 */
data class GenericApiResponse<T>(
    @SerializedName("message") val message: String? = null,
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("code") val code: Int = 200,
    @SerializedName("data") val data: T? = null
) : Serializable

data class RegisterPatientResponseData(
    @SerializedName("proceed") val proceed: Int?,
    @SerializedName("message") val message: String?
) : Serializable

data class VitalsAddResponseData(
    @SerializedName("id") val id: Long?,
    @SerializedName("patient_id") val patientId: String?,
    @SerializedName("slug") val slug: Int?,
    @SerializedName("message") val message: String?
) : Serializable

data class VisitAddResponseData(
    @SerializedName("slug") val slug: Int?,
    @SerializedName("message") val message: String?
) : Serializable

/* -------------------- Retrofit interface -------------------- */

interface ApiService {

    /**
     * Register a new patient
     * POST {base}/patients/register
     */
    @POST("patients/register")
    suspend fun registerPatient(
        @Body body: RegisterPatientRequest
    ): Response<GenericApiResponse<RegisterPatientResponseData>>

    /**
     * Add vitals
     * POST {base}/vital/add   (note: singular "vital" to match your sample)
     */
    @POST("vital/add")
    suspend fun addVital(
        @Body body: VitalsRequest
    ): Response<GenericApiResponse<VitalsAddResponseData>>

    /**
     * Add a visit/assessment
     * POST {base}/visits/add
     */
    @POST("visits/add")
    suspend fun addVisit(
        @Body body: VisitAddRequest
    ): Response<GenericApiResponse<VisitAddResponseData>>
}
