package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * GeneralAssessment
 *
 * Represents the assessment form captured when a patient's BMI is < 25.
 * - Multiple assessments per patient are allowed (on different visit dates).
 * - visitDateEpoch should be normalized to local-midnight if you want date-only uniqueness.
 *
 * Fields:
 *  - patientOwnerId: FK to local Patient.id
 *  - visitDateEpoch: epoch millis (the visit date)
 *  - generalHealth: "Good" | "Poor"
 *  - everOnDiet: whether the patient has ever been on a diet to lose weight
 *  - comments: free-text notes (mandatory)
 */
@Entity(
    tableName = "assessment_general",
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
data class GeneralAssessment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Local patient PK this assessment belongs to */
    val patientOwnerId: Long,

    /** Visit date stored as epoch milliseconds (normalize to midnight for date queries) */
    val visitDateEpoch: Long,

    /** General health value, expected values: "Good" or "Poor" */
    val generalHealth: String,

    /** True if the patient has ever been on a diet to lose weight */
    val everOnDiet: Boolean,

    /** Free-text comments (mandatory per spec) */
    val comments: String
)
