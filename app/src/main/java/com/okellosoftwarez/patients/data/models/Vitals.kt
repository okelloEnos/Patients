package com.okellosoftwarez.patients.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vitals",
    foreignKeys = [ForeignKey(entity = Patient::class, parentColumns = ["id"], childColumns = ["patientOwnerId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("patientOwnerId"), Index("visitDateEpoch")]
)
data class Vitals(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientOwnerId: Long,
    val visitDateEpoch: Long,
    val heightCm: Double,
    val weightKg: Double,
    val bmi: Double,
    // NEW: id returned by remote API after successful sync (nullable)
    val remoteId: Long? = null
)
