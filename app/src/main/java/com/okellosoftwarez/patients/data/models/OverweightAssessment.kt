package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * OverweightAssessment
 *
 * Represents the assessment form captured when a patient's BMI is >= 25.
 * - A patient can have multiple overweight assessments, each on a different visit date.
 * - visitDateEpoch should be normalized to local-midnight if you want date-only uniqueness.
 *
 * Fields:
 *  - patientOwnerId: FK to local Patient.id
 *  - visitDateEpoch: epoch millis (the visit date)
 *  - generalHealth: "Good" | "Poor"
 *  - usingDrugs: whether patient is currently using any drugs (boolean)
 *  - comments: free-text notes
 */
@Entity(
    tableName = "assessment_overweight",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientOwnerId"), Index("visitDateEpoch")]
)
data class OverweightAssessment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Local patient PK this assessment belongs to */
    val patientOwnerId: Long,

    /** Visit date stored as epoch milliseconds (normalize to midnight for date queries) */
    val visitDateEpoch: Long,

    /** General health value, expected values: \"Good\" or \"Poor\" */
    val generalHealth: String,

    /** True if the patient is currently using any drugs */
    val usingDrugs: Boolean,

    /** Free-text comments (mandatory per spec) */
    val comments: String
)
