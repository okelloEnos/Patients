package com.okellosoftwarez.patients.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.okellosoftwarez.patients.data.models.OverweightAssessment

/**
 * OverweightAssessmentDao
 *
 * DAO for managing overweight assessment form data.
 * - Each record corresponds to a patient visit when BMI ≥ 25.
 * - Supports insertion, retrieval by patient, and filtering by date.
 */
@Dao
interface OverweightAssessmentDao {

    /** Insert a new overweight assessment record. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(assessment: OverweightAssessment): Long

    /** Update an existing overweight assessment. */
    @Update
    suspend fun update(assessment: OverweightAssessment)

    /** Delete a specific overweight assessment. */
    @Delete
    suspend fun delete(assessment: OverweightAssessment)

    /**
     * Get all overweight assessments for a specific patient.
     * Ordered by most recent visit first.
     */
    @Query("""
        SELECT * FROM assessment_overweight
        WHERE patientOwnerId = :patientId
        ORDER BY visitDateEpoch DESC
    """)
    fun getAllForPatient(patientId: Long): LiveData<List<OverweightAssessment>>

    /**
     * Get the overweight assessment for a specific patient visit date (if exists).
     * Useful to prevent duplicate entries for the same visit.
     */
    @Query("""
        SELECT * FROM assessment_overweight
        WHERE patientOwnerId = :patientId AND visitDateEpoch = :visitDateEpoch
        LIMIT 1
    """)
    suspend fun getForPatientOnDate(patientId: Long, visitDateEpoch: Long): OverweightAssessment?

    /**
     * Return all overweight assessments recorded on a specific date.
     * Useful for filtering reports by visit date.
     */
    @Query("""
        SELECT * FROM assessment_overweight
        WHERE visitDateEpoch = :visitDateEpoch
        ORDER BY patientOwnerId ASC
    """)
    suspend fun getAllByVisitDate(visitDateEpoch: Long): List<OverweightAssessment>
}
