package com.okellosoftwarez.patients.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.okellosoftwarez.patients.util.DateUtils
import com.okellosoftwarez.patients.viewmodel.SharedViewModel
import com.okellosoftwarez.patients.viewmodel.UiEvent
import java.text.DecimalFormat

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientVitalsScreen(
    patientId: Long,
    onNavigate: (String) -> Unit = {},
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val df = remember { DecimalFormat("#0.0") }
    val patient by sharedViewModel.getPatientById(patientId).collectAsState()

    var visitEpoch by remember { mutableLongStateOf(DateUtils.todayEpoch()) }
    var heightCmText by remember { mutableStateOf("") }
    var weightKgText by remember { mutableStateOf("") }

    val visitInteractionSource = remember { MutableInteractionSource() }

    val endOfTodayMillis = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        cal.timeInMillis
    }

    val selectableUpToToday = remember(endOfTodayMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= endOfTodayMillis
            }
        }
    }

    val visitState = rememberDatePickerState(initialSelectedDateMillis = visitEpoch, selectableDates = selectableUpToToday)
    var showVisitPicker by remember { mutableStateOf(false) }

    val heightCm = heightCmText.toDoubleOrNull()?.takeIf { it > 0.0 }
    val weightKg = weightKgText.toDoubleOrNull()?.takeIf { it > 0.0 }

    val bmi: Double? = if (heightCm != null && weightKg != null && heightCm > 0.0) {
        val heightM = heightCm / 100.0
        (weightKg / (heightM * heightM)).takeIf { !it.isNaN() && !it.isInfinite() }
    } else null

    val isSaving by sharedViewModel.isSaving.collectAsState()
    LaunchedEffect(Unit) {
        sharedViewModel.events.collect { ev ->
            when (ev) {
                is UiEvent.ShowMessage -> Toast.makeText(context, ev.msg, Toast.LENGTH_SHORT).show()
                is UiEvent.Navigate -> onNavigate(ev.route)
            }
        }
    }

    LaunchedEffect(visitInteractionSource) {
        visitInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showVisitPicker = true
        }
    }

    var attemptedSubmit by remember { mutableStateOf(false) }

    val isVisitValid by derivedStateOf { visitEpoch != null && (visitEpoch ?: 0L) <= endOfTodayMillis }
    val isHeightValid by derivedStateOf { heightCm != null && heightCm in 20.0..300.0 } // reasonable range
    val isWeightValid by derivedStateOf { weightKg != null && weightKg in 2.0..500.0 }   // reasonable range

    val isFormValid by derivedStateOf {
        isVisitValid && isHeightValid && isWeightValid
    }

    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Vitals Assessment") }) },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNavigate("patients") },
                        modifier = Modifier.weight(2f).height(48.dp),
                        shape = MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(12.dp)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text("Close", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            attemptedSubmit = true
                            if (!isFormValid) {
                                Toast.makeText(context, "Please fill all required details before saving", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val visitMillis = visitEpoch
                            sharedViewModel.saveVitalsAndNavigate(patientId, visitMillis, heightCm!!, weightKg!!)
                        },
                        enabled = isFormValid && !isSaving,
                        modifier = Modifier.weight(3f).height(48.dp),
                        shape = MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(12.dp)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text("Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val displayName = patient?.let { "${it.firstName} ${it.lastName}" } ?: ""

            Column {
                Text("Patient's Name", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    placeholder = { Text("Patient's name", color = hintColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text("Visit Date *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = visitEpoch.let { DateUtils.toDisplayDate(it) },
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select visit date", color = hintColor) },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Visit date") },
                    interactionSource = visitInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                if (attemptedSubmit && !isVisitValid) {
                    Text(
                        "Visit date is required and cannot be in the future",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (showVisitPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showVisitPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selected = visitState.selectedDateMillis
                                if (selected != null && selected <= endOfTodayMillis) {
                                    visitEpoch = DateUtils.normalizeToMidnight(selected)
                                } else if (selected != null) {
                                    Toast.makeText(context, "Cannot select a future date", Toast.LENGTH_SHORT).show()
                                }
                                showVisitPicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showVisitPicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = visitState)
                    }
                }
            }

            Column {
                Text("Height (cm) *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = heightCmText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        heightCmText = filtered
                    },
                    placeholder = { Text("e.g 170", color = hintColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (attemptedSubmit && !isHeightValid) {
                    Text(
                        "Enter a valid height (20 - 300 cm)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Column {
                Text("Weight (kg) *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = weightKgText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        weightKgText = filtered
                    },
                    placeholder = { Text("e.g. 70", color = hintColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (attemptedSubmit && !isWeightValid) {
                    Text(
                        "Enter a valid weight (2 - 500 kg)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Column {
                Text("BMI", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = bmi?.let { df.format(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text("", color = hintColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
