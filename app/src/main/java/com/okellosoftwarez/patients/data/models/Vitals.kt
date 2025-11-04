package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Vitals
 *
 * Stores a patient's recorded physical metrics for a given visit date.
 * BMI is calculated automatically in the app before saving.
 *
 * Each patient can have multiple vitals entries (one per visit date).
 */
@Entity(
    tableName = "vitals",
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
data class Vitals(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Foreign key link to the local patient record */
    val patientOwnerId: Long,

    /** The visit date stored as epoch milliseconds */
    val visitDateEpoch: Long,

    /** Height in centimeters */
    val heightCm: Double,

    /** Weight in kilograms */
    val weightKg: Double,

    /** Body Mass Index (BMI = kg / (m^2)) */
    val bmi: Double
)
