package com.okellosoftwarez.patients.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.okellosoftwarez.patients.data.models.Vitals

@Dao
interface VitalsDao {

    /** Insert a new vitals record. Returns the generated row id. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vitals: Vitals): Long

    /** Update an existing vitals record. */
    @Update
    suspend fun update(vitals: Vitals)

    /** Delete a vitals record. */
    @Delete
    suspend fun delete(vitals: Vitals)

    /**
     * Return the latest vitals record for a patient (ordered by visitDateEpoch desc).
     * Useful for showing the 'last BMI' on patient lists.
     */
    @Query("SELECT * FROM vitals WHERE patientOwnerId = :patientId ORDER BY visitDateEpoch DESC LIMIT 1")
    suspend fun getLatestForPatient(patientId: Long): Vitals?

    /**
     * Check if a vitals record already exists for given patient on the same visit date (epoch).
     * Returns count (0 = none, >0 = exists).
     *
     * Note: You should normalize visitDateEpoch to local midnight when doing date-only comparisons.
     */
    @Query("SELECT COUNT(*) FROM vitals WHERE patientOwnerId = :patientId AND visitDateEpoch = :visitDateEpoch")
    suspend fun existsForPatientOnDate(patientId: Long, visitDateEpoch: Long): Int

    /**
     * Get all vitals entries that occurred on a specific visit date.
     * Useful to filter patients by visit date (reporting).
     */
    @Query("SELECT * FROM vitals WHERE visitDateEpoch = :visitDateEpoch")
    suspend fun getVitalsByDate(visitDateEpoch: Long): List<Vitals>

    /**
     * LiveData stream of all vitals for a patient (ordered newest first).
     * Useful when composing a UI that observes a patient's vitals history.
     */
    @Query("SELECT * FROM vitals WHERE patientOwnerId = :patientId ORDER BY visitDateEpoch DESC")
    fun getAllForPatientLive(patientId: Long): LiveData<List<Vitals>>
}
