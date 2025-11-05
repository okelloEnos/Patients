package com.okellosoftwarez.patients.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.okellosoftwarez.patients.data.dao.PatientWithLastVitals
import com.okellosoftwarez.patients.data.models.*
import com.okellosoftwarez.patients.repository.PatientRepository
import com.okellosoftwarez.patients.util.BmiUtils
import com.okellosoftwarez.patients.worker.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class UiEvent {
    data class ShowMessage(val msg: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: PatientRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val patients: LiveData<List<com.okellosoftwarez.patients.data.models.Patient>> = repo.getAllPatients()
    val patientsWithLastVitals: LiveData<List<PatientWithLastVitals>> = repo.getPatientsWithLastVitals()

    private val _pendingSyncCount = MutableLiveData(0)
    val pendingSyncCount: LiveData<Int> = _pendingSyncCount

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        refreshPendingCount()
    }

    fun refreshPendingCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = repo.getPendingCount()
                _pendingSyncCount.postValue(count)
            } catch (_: Exception) {
            }
        }
    }

    fun getPatientsByVisitDate(visitDateEpoch: Long): LiveData<List<PatientWithLastVitals>> {
        return repo.getPatientsByVisitDate(visitDateEpoch)
    }

    fun getPatientById(patientId: Long): StateFlow<com.okellosoftwarez.patients.data.models.Patient?> {
        val result = MutableStateFlow<com.okellosoftwarez.patients.data.models.Patient?>(null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val p = repo.getPatientById(patientId)
                result.value = p
            } catch (_: Exception) {
            }
        }
        return result
    }

    fun registerPatient(
        patientId: String,
        registrationDateEpoch: Long,
        firstName: String,
        lastName: String,
        dobEpoch: Long?,
        gender: String?
    ) {
        viewModelScope.launch {
            _isSaving.emit(true)
            try {
                if (patientId.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                    _events.tryEmit(UiEvent.ShowMessage("Please complete all required fields"))
                    return@launch
                }

                val existing = repo.countByPatientId(patientId)
                if (existing > 0) {
                    _events.tryEmit(UiEvent.ShowMessage("Patient ID already exists"))
                    return@launch
                }

                val patient = Patient(
                    patientId = patientId,
                    registrationDateEpoch = registrationDateEpoch,
                    firstName = firstName,
                    lastName = lastName,
                    dobEpoch = dobEpoch,
                    gender = gender
                )

                val localId = repo.insertPatient(patient)

                // refresh pending count (in case network queued something)
                refreshPendingCount()

                _events.tryEmit(UiEvent.Navigate("vitals/$localId"))
            } catch (ex: Exception) {
                _events.tryEmit(UiEvent.ShowMessage("Failed to register patient: ${ex.message ?: "unknown"}"))
            } finally {
                _isSaving.emit(false)
            }
        }
    }

    fun requestSync() {
        viewModelScope.launch {
            val work = OneTimeWorkRequestBuilder<SyncWorker>().build()
            val wm = WorkManager.getInstance(getApplication())
            wm.enqueueUniqueWork("sync_worker_now", ExistingWorkPolicy.KEEP, listOf(work))

            _events.emit(UiEvent.ShowMessage("Sync queued"))

            // observe the work lifecycle
            wm.getWorkInfoByIdLiveData(work.id).observeForever { info ->
                if (info != null) {
                    _isSyncing.value = info.state == androidx.work.WorkInfo.State.RUNNING
                    // optionally emit messages on SUCCEEDED / FAILED
                    if (info.state.isFinished) {
                        viewModelScope.launch { _events.emit(UiEvent.ShowMessage("Sync ${info.state}")) }
                    }
                }
            }
        }
    }

    fun saveVitalsAndNavigate(
        patientLocalId: Long,
        visitDateEpoch: Long,
        heightCm: Double,
        weightKg: Double
    ) {
        viewModelScope.launch {
            _isSaving.emit(true)
            try {
                if (heightCm <= 0.0 || weightKg <= 0.0) {
                    _events.tryEmit(UiEvent.ShowMessage("Height and weight must be greater than zero"))
                    return@launch
                }

                val exists = repo.existsVitalsForPatientOnDate(patientLocalId, visitDateEpoch)
                if (exists > 0) {
                    _events.tryEmit(UiEvent.ShowMessage("Vitals for this date already exist"))
                    return@launch
                }

                val bmi = BmiUtils.calculateBmi(weightKg, heightCm)
                val vitals = Vitals(
                    patientOwnerId = patientLocalId,
                    visitDateEpoch = visitDateEpoch,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    bmi = bmi
                )

                repo.saveVitals(vitals)
                refreshPendingCount()

                if (bmi < 25.0) {
                    _events.tryEmit(UiEvent.Navigate("general/$patientLocalId/$visitDateEpoch"))
                } else {
                    _events.tryEmit(UiEvent.Navigate("overweight/$patientLocalId/$visitDateEpoch"))
                }
            } catch (ex: Exception) {
                _events.tryEmit(UiEvent.ShowMessage("Failed to save vitals: ${ex.message ?: "unknown"}"))
            } finally {
                _isSaving.emit(false)
            }
        }
    }

    fun saveGeneralAssessment(
        patientLocalId: Long,
        visitDateEpoch: Long,
        generalHealth: String,
        everOnDiet: Boolean,
        comments: String
    ) {
        viewModelScope.launch {
            _isSaving.emit(true)
            try {
                if (generalHealth.isBlank() || comments.isBlank()) {
                    _events.tryEmit(UiEvent.ShowMessage("Please complete all required fields"))
                    return@launch
                }

                val assessment = GeneralAssessment(
                    patientOwnerId = patientLocalId,
                    visitDateEpoch = visitDateEpoch,
                    generalHealth = generalHealth,
                    everOnDiet = everOnDiet,
                    comments = comments
                )

                repo.saveGeneralAssessment(assessment)
                refreshPendingCount()
                _events.tryEmit(UiEvent.Navigate("patients"))
            } catch (ex: Exception) {
                _events.tryEmit(UiEvent.ShowMessage("Failed to save assessment: ${ex.message ?: "unknown"}"))
            } finally {
                _isSaving.emit(false)
            }
        }
    }

    fun saveOverweightAssessment(
        patientLocalId: Long,
        visitDateEpoch: Long,
        generalHealth: String,
        usingDrugs: Boolean,
        comments: String
    ) {
        viewModelScope.launch {
            _isSaving.emit(true)
            try {
                if (generalHealth.isBlank() || comments.isBlank()) {
                    _events.tryEmit(UiEvent.ShowMessage("Please complete all required fields"))
                    return@launch
                }

                val assessment = OverweightAssessment(
                    patientOwnerId = patientLocalId,
                    visitDateEpoch = visitDateEpoch,
                    generalHealth = generalHealth,
                    usingDrugs = usingDrugs,
                    comments = comments
                )

                repo.saveOverweightAssessment(assessment)
                refreshPendingCount()
                _events.tryEmit(UiEvent.Navigate("patients"))
            } catch (ex: Exception) {
                _events.tryEmit(UiEvent.ShowMessage("Failed to save overweight assessment: ${ex.message ?: "unknown"}"))
            } finally {
                _isSaving.emit(false)
            }
        }
    }
}
