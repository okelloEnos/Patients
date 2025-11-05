//package com.okellosoftwarez.patients.worker
//
//import com.okellosoftwarez.patients.data.models.Patient
//import com.okellosoftwarez.patients.repository.PatientRepository
//import com.okellosoftwarez.patients.network.ApiService
//import com.okellosoftwarez.patients.util.DateUtils
//import com.google.gson.Gson
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.time.Instant
//import java.time.format.DateTimeParseException
//import javax.inject.Inject
//import javax.inject.Singleton
//
///**
// * Result of the initial sync operation.
// *
// * - inserted: number of remote patients inserted locally
// * - skippedExisting: number of remote patients skipped because a local record already exists
// */
//data class InitialSyncResult(
//    val inserted: Int,
//    val skippedExisting: Int
//)
//
//@Singleton
//class InitialSyncUseCase @Inject constructor(
//    private val api: ApiService,
//    private val repo: PatientRepository,
//    private val gson: Gson
//) {
//
//    /**
//     * Run the initial sync:
//     *  - fetch remote patients via ApiService.listPatients()
//     *  - map each remote record to Patient entity
//     *  - insert into local DB only if patientId does not exist locally
//     *
//     * Note: merge policy = prefer local (skip if local record exists).
//     */
//    suspend fun runInitialSync(): Result<InitialSyncResult> = withContext(Dispatchers.IO) {
//        return@withContext try {
//            val response = api.listPatients()
//
//            if (!response.isSuccessful) {
//                return@withContext Result.failure(
//                    Exception("Failed to fetch remote patients: HTTP ${response.code()}")
//                )
//            }
//
//            val body = response.body() ?: emptyList<Map<String, Any>>()
//
//            var inserted = 0
//            var skipped = 0
//
//            for (raw in body) {
//                // Extract canonical patient identifier from several possible keys.
//                val patientId = extractString(raw, "patient_id")
//                    ?: extractString(raw, "patientId")
//                    ?: extractString(raw, "id")
//                    ?: run {
//                        // skip records that do not contain a stable external id
//                        skipped++
//                        continue
//                    }
//
//                // If local already has this patient, skip (prefer local)
//                val exists = repo.countByPatientId(patientId)
//                if (exists > 0) {
//                    skipped++
//                    continue
//                }
//
//                // Map other fields (best-effort)
//                val firstName = extractString(raw, "first_name")
//                    ?: extractString(raw, "firstName")
//                    ?: extractString(raw, "firstname")
//                    ?: ""
//
//                val lastName = extractString(raw, "last_name")
//                    ?: extractString(raw, "lastName")
//                    ?: extractString(raw, "lastname")
//                    ?: ""
//
//                // registration date: try several shapes (epoch long or ISO string)
//                val registrationEpoch = parseDateToEpoch(raw, "registration_date")
//                    ?: parseDateToEpoch(raw, "registrationDate")
//                    ?: parseDateToEpoch(raw, "registered_at")
//                    ?: System.currentTimeMillis()
//
//                // Optional: dob, gender
//                val dobEpoch = parseDateToEpoch(raw, "dob") ?: parseDateToEpoch(raw, "date_of_birth")
//                val gender = extractString(raw, "gender") ?: extractString(raw, "sex")
//
//                // Build Patient entity and insert
//                val patient = Patient(
//                    patientId = patientId,
//                    registrationDateEpoch = registrationEpoch,
//                    firstName = firstName,
//                    lastName = lastName,
//                    dobEpoch = dobEpoch,
//                    gender = gender
//                )
//
//                repo.insertPatient(patient)
//                inserted++
//            }
//
//            Result.success(InitialSyncResult(inserted = inserted, skippedExisting = skipped))
//        } catch (ex: Exception) {
//            Result.failure(ex)
//        }
//    }
//
//    /**
//     * Try to parse a date field from the raw map into epoch millis.
//     *
//     * Supported value shapes:
//     *  - Number (Long/Double) -> treated as epoch millis (or epoch seconds if small)
//     *  - String -> try ISO-8601 parse; if numeric string attempt to parse as long
//     */
//    private fun parseDateToEpoch(map: Map<String, Any>, key: String): Long? {
//        val v = map[key] ?: return null
//
//        // If it's a number, interpret as epoch millis (or seconds -> convert)
//        when (v) {
//            is Number -> {
//                val value = v.toLong()
//                // Heuristic: if value looks like seconds (10-digit), convert to millis
//                return if (value < 10000000000L) value * 1000L else value
//            }
//            is String -> {
//                val s = v.trim()
//                // numeric string?
//                s.toLongOrNull()?.let { n ->
//                    return if (n < 10000000000L) n * 1000L else n
//                }
//
//                // Try ISO parse
//                try {
//                    // Use Instant.parse for strict ISO-8601 with offset/z
//                    val instant = Instant.parse(s)
//                    return instant.toEpochMilli()
//                } catch (e: DateTimeParseException) {
//                    // Try a looser ISO parsing using DateUtils if available
//                    try {
//                        // DateUtils.toEpoch may exist in your util; otherwise attempt to parse date-only
//                        val iso = DateUtils.parseIsoToEpoch(s)
//                        if (iso != null && iso > 0L) return iso
//                    } catch (_: Exception) {
//                        // last-resort: ignore
//                    }
//                }
//            }
//        }
//        return null
//    }
//
//    /**
//     * Safe extractor for string fields in the remote map.
//     */
//    private fun extractString(map: Map<String, Any>, key: String): String? {
//        val v = map[key] ?: return null
//        return when (v) {
//            is String -> v.trim().ifEmpty { null }
//            is Number -> v.toString()
//            else -> {
//                // Attempt to convert via Gson for nested primitives
//                try {
//                    gson.toJson(v)
//                } catch (_: Exception) {
//                    null
//                }
//            }
//        }
//    }
//}
