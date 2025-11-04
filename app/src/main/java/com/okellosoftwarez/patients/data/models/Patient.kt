package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Patient
 *
 * Represents a registered patient in the system.
 *
 * Requirements:
 *  - Each patient must have a unique Patient ID.
 *  - A patient can only be registered once.
 *  - When saved, this record is linked with vitals and assessment data.
 */
@Entity(
    tableName = "patients",
    indices = [Index(value = ["patientId"], unique = true)]
)
data class Patient(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** The unique identifier for the patient (cannot be shared). */
    val patientId: String,

    /** Date of registration, stored as epoch milliseconds. */
    val registrationDateEpoch: Long,

    /** Patient’s first name. */
    val firstName: String,

    /** Patient’s last name. */
    val lastName: String,

    /** Date of birth (optional), stored as epoch milliseconds. */
    val dobEpoch: Long? = null,

    /** Gender (e.g., "Male", "Female", "Other"). */
    val gender: String? = null
)

