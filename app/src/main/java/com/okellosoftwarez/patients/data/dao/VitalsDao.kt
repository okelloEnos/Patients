package com.okellosoftwarez.patients.data.dao

import androidx.room.*
import com.okellosoftwarez.patients.data.models.Patient
import com.okellosoftwarez.patients.data.models.Vitals

@Dao
interface VitalsDao {
    @Insert
    suspend fun insert(vitals: Vitals): Long
//h
    @Update
    suspend fun update(vitals: Vitals)

    @Query("SELECT * FROM vitals WHERE patientOwnerId = :patientId ORDER BY visitDateEpoch DESC LIMIT 1")
    suspend fun getLatestForPatient(patientId: Long): Vitals?

    @Query("SELECT COUNT(*) FROM vitals WHERE patientOwnerId = :patientId AND visitDateEpoch = :visitDateEpoch")
    suspend fun existsForPatientOnDate(patientId: Long, visitDateEpoch: Long): Int

    @Query("SELECT * FROM vitals WHERE visitDateEpoch = :visitDateEpoch")
    suspend fun getVitalsByDate(visitDateEpoch: Long): List<Vitals>

    // NEW: find the vitals row for a given patient local id and visit date (exact match)
    @Query("SELECT * FROM vitals WHERE patientOwnerId = :patientId AND visitDateEpoch = :visitDateEpoch LIMIT 1")
//    @Query("SELECT * FROM vitals WHERE patientOwnerId = 1 AND visitDateEpoch = 1758315600000 LIMIT 1")
    suspend fun findByPatientAndDate(patientId: Long, visitDateEpoch: Long): Vitals?

    /** Get patient by local DB id. */
    @Query("SELECT * FROM vitals WHERE id = :id LIMIT 1")
    suspend fun getVitalById(id: Long): Vitals?
}
