package com.okellosoftwarez.patients.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.*

//@Composable
//fun PatientListScreen(
//    onNavigate: (String) -> Unit,
////    sharedViewModel: SharedViewModel = viewModel()
//) {
//    val patients = []
//    // Observe patients LiveData from Room via AppModule singleton
//    val patients by AppModule.db.patientDao().getAllPatients().observeAsState(emptyList())
//
//    // Map to keep latest vitals for each patient (patientLocalId -> Vitals?)
//    val latestVitalsMap = remember { mutableStateMapOf<Long, com.example.patientapp.data.models.Vitals?>() }
//
//    // Filter by visit date (yyyy-MM-dd string)
//    var filterDateStr by remember { mutableStateOf("") }
//    var isFiltering by remember { mutableStateOf(false) }
//    val filteredPatientIds = remember { mutableStateListOf<Long>() }
//
//    // Whenever the patients list changes, prefetch latest vitals for each patient in background
//    LaunchedEffect(patients) {
//        for (p in patients) {
//            // launch per-patient fetch to avoid blocking the UI
//            launch {
//                val v = AppModule.db.vitalsDao().getLatestForPatient(p.id)
//                latestVitalsMap[p.id] = v
//            }
//        }
//    }
//
//    // When filter date changes, query vitals for that date and compute patient ids to display
//    LaunchedEffect(filterDateStr) {
//        if (filterDateStr.isBlank()) {
//            isFiltering = false
//            filteredPatientIds.clear()
//        } else {
//            isFiltering = true
//            // parse yyyy-MM-dd
//            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
//            val dateEpoch = try { sdf.parse(filterDateStr)?.time ?: 0L } catch (e: Exception) { 0L }
//            val vitalsOnDate = AppModule.db.vitalsDao().getVitalsByDate(dateEpoch)
//            val ids = vitalsOnDate.map { it.patientOwnerId }.distinct()
//            filteredPatientIds.clear()
//            filteredPatientIds.addAll(ids)
//            // prefetch latest vitals for ids
//            for (pid in ids) {
//                launch {
//                    val v = AppModule.db.vitalsDao().getLatestForPatient(pid)
//                    latestVitalsMap[pid] = v
//                }
//            }
//        }
//    }
//
//    Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                OutlinedTextField(
//                    value = filterDateStr,
//                    onValueChange = { filterDateStr = it },
//                    label = { Text("Filter by visit date (yyyy-MM-dd)") },
//                    modifier = Modifier.weight(1f)
//                )
//                Button(onClick = { filterDateStr = "" }) { Text("Clear") }
//            }
//
//            Text(text = "Patients (${patients.size})", fontWeight = FontWeight.Bold)
//
//            val toDisplay: List<Patient> = if (isFiltering && filteredPatientIds.isNotEmpty()) {
//                patients.filter { filteredPatientIds.contains(it.id) }
//            } else {
//                patients
//            }
//
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
//                items(toDisplay, key = { it.id }) { patient ->
//                    val latest = latestVitalsMap[patient.id]
//                    val bmi = latest?.bmi ?: 0.0
//                    val bmiText = if (latest != null) String.format(Locale.US, "%.2f (%s)", bmi, BmiUtils.bmiCategory(bmi)) else "-"
//
//                    PatientListItem(
//                        patient = patient,
//                        age = computeAgeYears(patient.dobEpoch),
//                        lastBmiText = bmiText,
//                        onClick = { onNavigate("vitals/${patient.id}") }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun PatientListItem(patient: Patient, age: String, lastBmiText: String, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .background(Color(0xFFF6F6F6))
//            .padding(12.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(modifier = Modifier.weight(1f)) {
//            Text(text = "${patient.firstName} ${patient.lastName}", fontWeight = FontWeight.SemiBold)
//            Text(text = "Age: $age")
//        }
//        Column(horizontalAlignment = Alignment.End) {
//            Text(text = "Last BMI: $lastBmiText", fontWeight = FontWeight.Medium)
//            Text(text = "ID: ${patient.patientId}")
//        }
//    }
//}
//
//private fun computeAgeYears(dobEpoch: Long?): String {
//    if (dobEpoch == null || dobEpoch <= 0L) return "-"
//    val dob = Calendar.getInstance().apply { timeInMillis = dobEpoch }
//    val now = Calendar.getInstance()
//    var years = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
//    if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) years -= 1
//    return years.toString()
//}

@Composable
fun PatientListScreen(
    onNavigate: (String) -> Unit,
//    vm: SharedViewModel = viewModel()
) {
//    // For demo, we'll fetch LiveData via repository directly (production: expose via ViewModel)
//    val patientsLive = AppModule.db.patientDao().getAllPatients()
//    // This is a quick hack: in Compose we usually observe LiveData with observeAsState; but for brevity we skip

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Patient Listing")
        Spacer(modifier = Modifier.height(8.dp))
        // If you want filters, add date picker and call appropriate query
        Text("(List coming from Room)")
//        Spacer(modifier = Modifier.height(12.dp))
//        // Basic manual view placeholder
//        Button(onClick = { /* trigger manual sync */ }) { Text("Retry pending sync") }
    }
}
