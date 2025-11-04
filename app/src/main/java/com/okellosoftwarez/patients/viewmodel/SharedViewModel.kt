package com.okellosoftwarez.patients.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
//import com.example.patientapp.data.models.*
//import com.example.patientapp.repository.PatientRepository
//import com.example.patientapp.util.BmiUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SharedViewModel
 *
 * Responsible for:
 * - registering patients (enforcing unique patientId)
 * - saving vitals and routing to the correct assessment screen
 * - saving assessment forms
 * - exposing patients LiveData for UI lists
 *
 * Uses a MutableSharedFlow of UiEvent to notify the UI about navigation or messages.
 */
sealed class UiEvent {
    data class ShowMessage(val msg: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

@HiltViewModel
class SharedViewModel @Inject constructor(
//    private val repo: PatientRepository,
    application: Application
) : AndroidViewModel(application) {

//    // Events for the UI (navigation/snackbar/etc)
//    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
//    val events = _events.asSharedFlow()
//
//    // Expose patients list as LiveData so Compose screens / Activities can observe it
//    // (Room returns LiveData from the DAO; repository forwards it)
//    val patients: LiveData<List<Patient>> = repo.getAllPatients()
//
//    /**
//     * Register a new patient, ensuring patientId uniqueness locally.
//     * On success navigate to vitals/{patientLocalId}.
//     */
//    fun registerPatient(
//        patientId: String,
//        registrationDateEpoch: Long,
//        firstName: String,
//        lastName: String,
//        dobEpoch: Long?,
//        gender: String
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val count = repo.countByPatientId(patientId)
//                if (count > 0) {
//                    _events.tryEmit(UiEvent.ShowMessage("Patient ID already exists"))
//                    return@launch
//                }
//
//                val patient = Patient(
//                    patientId = patientId,
//                    registrationDateEpoch = registrationDateEpoch,
//                    firstName = firstName,
//                    lastName = lastName,
//                    dobEpoch = dobEpoch,
//                    gender = gender
//                )
//
//                val id = repo.insertPatient(patient)
//                // Navigate to Vitals screen for newly created local patient id
//                _events.tryEmit(UiEvent.Navigate("vitals/$id"))
//            } catch (ex: Exception) {
//                _events.tryEmit(UiEvent.ShowMessage("Failed to register patient: ${ex.message ?: "unknown error"}"))
//            }
//        }
//    }
//
//    /**
//     * Save vitals for a patient and navigate to the correct assessment screen
//     * based on BMI: BMI < 25 -> General; BMI >= 25 -> Overweight
//     *
//     * Enforces per-patient / per-date uniqueness (prevents multiple vitals on same date).
//     */
//    fun saveVitalsAndNavigate(
//        patientLocalId: Long,
//        visitDateEpoch: Long,
//        heightCm: Double,
//        weightKg: Double
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // check for existing vitals on same date
//                val exists = repo.getLatestVitals(patientLocalId)?.let {
//                    // If latest has same visitDateEpoch then consider conflict.
//                    // However we also have DAO existsForPatientOnDate - prefer that if available:
//                    null
//                }
//
//                // use DAO direct check for date uniqueness (repo has helper via DB)
//                val collision = com.example.patientapp.data.AppDatabase
//                    .getInstance(getApplication())
//                    .vitalsDao()
//                    .existsForPatientOnDate(patientLocalId, visitDateEpoch)
//                if (collision > 0) {
//                    _events.tryEmit(UiEvent.ShowMessage("Vitals for this date already exist"))
//                    return@launch
//                }
//
//                // Calculate BMI correctly: height in cm -> meters before squaring.
//                val bmi = BmiUtils.calculateBmi(weightKg, heightCm)
//
//                val vitals = Vitals(
//                    patientOwnerId = patientLocalId,
//                    visitDateEpoch = visitDateEpoch,
//                    heightCm = heightCm,
//                    weightKg = weightKg,
//                    bmi = bmi
//                )
//
//                repo.saveVitals(vitals)
//
//                // Route: BMI < 25 => general, else overweight
//                if (bmi < 25.0) {
//                    _events.tryEmit(UiEvent.Navigate("general/$patientLocalId/$visitDateEpoch"))
//                } else {
//                    _events.tryEmit(UiEvent.Navigate("overweight/$patientLocalId/$visitDateEpoch"))
//                }
//            } catch (ex: Exception) {
//                _events.tryEmit(UiEvent.ShowMessage("Failed to save vitals: ${ex.message ?: "unknown error"}"))
//            }
//        }
//    }
//
//    /**
//     * Save general assessment (for BMI < 25). All fields are expected to be validated by UI.
//     * After saving navigate to the patient listing page.
//     */
//    fun saveGeneralAssessment(
//        patientLocalId: Long,
//        visitDateEpoch: Long,
//        generalHealth: String,
//        everOnDiet: Boolean,
//        comments: String
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val assessment = GeneralAssessment(
//                    patientOwnerId = patientLocalId,
//                    visitDateEpoch = visitDateEpoch,
//                    generalHealth = generalHealth,
//                    everOnDiet = everOnDiet,
//                    comments = comments
//                )
//                repo.saveGeneralAssessment(assessment)
//                _events.tryEmit(UiEvent.Navigate("patients"))
//            } catch (ex: Exception) {
//                _events.tryEmit(UiEvent.ShowMessage("Failed to save assessment: ${ex.message ?: "unknown error"}"))
//            }
//        }
//    }
//
//    /**
//     * Save overweight assessment (for BMI >= 25). After saving navigate to the patient listing.
//     */
//    fun saveOverweightAssessment(
//        patientLocalId: Long,
//        visitDateEpoch: Long,
//        generalHealth: String,
//        usingDrugs: Boolean,
//        comments: String
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val assessment = OverweightAssessment(
//                    patientOwnerId = patientLocalId,
//                    visitDateEpoch = visitDateEpoch,
//                    generalHealth = generalHealth,
//                    usingDrugs = usingDrugs,
//                    comments = comments
//                )
//                repo.saveOverweightAssessment(assessment)
//                _events.tryEmit(UiEvent.Navigate("patients"))
//            } catch (ex: Exception) {
//                _events.tryEmit(UiEvent.ShowMessage("Failed to save overweight assessment: ${ex.message ?: "unknown error"}"))
//            }
//        }
//    }
}
