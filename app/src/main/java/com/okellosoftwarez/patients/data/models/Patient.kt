package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "patients",
    indices = [Index(value = ["patientId"], unique = true)]
)
data class Patient(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val patientId: String,

    val registrationDateEpoch: Long,

    val firstName: String,

    val lastName: String,

    val dobEpoch: Long? = null,

    val gender: String? = null
)

