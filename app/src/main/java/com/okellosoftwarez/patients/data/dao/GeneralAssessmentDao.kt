package com.okellosoftwarez.patients.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.okellosoftwarez.patients.data.models.GeneralAssessment

/**
 * GeneralAssessmentDao
 *
 * DAO for managing general assessment form data (BMI < 25).
 * - Each record belongs to a single patient and a unique visit date.
 * - Multiple entries per patient allowed (one per different visit date).
 */
@Dao
interface GeneralAssessmentDao {

    /** Insert a new general assessment record. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(assessment: GeneralAssessment): Long

    /** Update an existing general assessment record. */
    @Update
    suspend fun update(assessment: GeneralAssessment)

    /** Delete a specific general assessment record. */
    @Delete
    suspend fun delete(assessment: GeneralAssessment)

    /**
     * Get all general assessments for a given patient.
     * Ordered by most recent visit date first.
     */
    @Query("""
        SELECT * FROM assessment_general
        WHERE patientOwnerId = :patientId
        ORDER BY visitDateEpoch DESC
    """)
    fun getAllForPatient(patientId: Long): LiveData<List<GeneralAssessment>>

    /**
     * Check if a general assessment exists for a patient on a specific visit date.
     * Prevents duplicate entries on the same date.
     */
    @Query("""
        SELECT COUNT(*) FROM assessment_general
        WHERE patientOwnerId = :patientId AND visitDateEpoch = :visitDateEpoch
    """)
    suspend fun existsForPatientOnDate(patientId: Long, visitDateEpoch: Long): Int

    /**
     * Retrieve all assessments submitted on a given visit date.
     * Useful for filtering or reporting by date.
     */
    @Query("""
        SELECT * FROM assessment_general
        WHERE visitDateEpoch = :visitDateEpoch
        ORDER BY patientOwnerId ASC
    """)
    suspend fun getAllByVisitDate(visitDateEpoch: Long): List<GeneralAssessment>
}
