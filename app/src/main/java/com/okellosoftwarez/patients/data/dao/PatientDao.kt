package com.okellosoftwarez.patients.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Embedded
import androidx.room.ColumnInfo
import com.okellosoftwarez.patients.data.models.Patient

/**
 * A convenience holder combining Patient + the latest vitals (if present).
 * Room maps the patient's columns into the embedded Patient object and
 * maps the aliased columns (lastBmi, lastVisitDateEpoch) into the properties below.
 */
data class PatientWithLastVitals(
    @Embedded val patient: Patient,
    @ColumnInfo(name = "lastBmi") val lastBmi: Double?,
    @ColumnInfo(name = "lastVisitDateEpoch") val lastVisitDateEpoch: Long?
)

@Dao
interface PatientDao {

    /** Insert a patient. If patientId must be unique, handle conflicts at app level (or change strategy). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(patient: Patient): Long

    @Update
    suspend fun update(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)

    /** Count patients with a given external patientId (used to enforce uniqueness). */
    @Query("SELECT COUNT(*) FROM patients WHERE patientId = :patientId")
    suspend fun countByPatientId(patientId: String): Int

    /** Get patient by local DB id. */
    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: Long): Patient?

    /** Simple LiveData list of all patients (no vitals info). */
    @Query("SELECT * FROM patients ORDER BY lastName, firstName")
    fun getAllPatients(): LiveData<List<Patient>>

    /**
     * Returns a LiveData list of patients plus their latest vitals (if any).
     *
     * Query explanation:
     * - For each patient p, we left join to the single vitals row v that has the latest visitDateEpoch
     *   for that patient. We achieve this by selecting the vitals row whose id matches the id from
     *   the subquery that orders vitals for that patient descending by visitDateEpoch and limits to 1.
     */
    @Transaction
    @Query(
        """
        SELECT p.*, v.bmi AS lastBmi, v.visitDateEpoch AS lastVisitDateEpoch
        FROM patients p
        LEFT JOIN vitals v
          ON v.id = (
            SELECT id FROM vitals v2
            WHERE v2.patientOwnerId = p.id
            ORDER BY v2.visitDateEpoch DESC
            LIMIT 1
          )
        ORDER BY p.lastName, p.firstName
        """
    )
    fun getPatientsWithLastVitals(): LiveData<List<PatientWithLastVitals>>

    /**
     * Filter patients who had a vitals visit on a specific visit date (visitDateEpoch).
     * Returns patient rows joined with the vitals row for that date (so lastBmi will be the bmi
     * from that visit).
     *
     * Note: visitDateEpoch should be normalized to local-midnight if you want date-only filtering.
     */
    @Transaction
    @Query(
        """
        SELECT p.*, v.bmi AS lastBmi, v.visitDateEpoch AS lastVisitDateEpoch
        FROM patients p
        JOIN vitals v ON p.id = v.patientOwnerId
        WHERE v.visitDateEpoch = :visitDateEpoch
        ORDER BY p.lastName, p.firstName
        """
    )
    fun getPatientsByVisitDate(visitDateEpoch: Long): LiveData<List<PatientWithLastVitals>>
}
