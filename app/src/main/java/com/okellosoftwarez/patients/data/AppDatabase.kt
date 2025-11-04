package com.okellosoftwarez.patients.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.okellosoftwarez.patients.data.dao.*
import com.okellosoftwarez.patients.data.models.*

/**
 * AppDatabase
 *
 * The central Room database for the Patient App.
 * It defines all entities and their DAOs.
 *
 * Entities:
 *  - Patient
 *  - Vitals
 *  - GeneralAssessment
 *  - OverweightAssessment
 *  - PendingSync (for offline sync queue)
 *
 * Access via Hilt-injected AppDatabase or static getInstance().
 */
@Database(
    entities = [
        Patient::class,
        Vitals::class,
        GeneralAssessment::class,
        OverweightAssessment::class,
        PendingSync::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun vitalsDao(): VitalsDao
    abstract fun generalAssessmentDao(): GeneralAssessmentDao
    abstract fun overweightAssessmentDao(): OverweightAssessmentDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns a singleton instance of the Room database.
         * Ensures only one instance exists app-wide.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "patient_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
