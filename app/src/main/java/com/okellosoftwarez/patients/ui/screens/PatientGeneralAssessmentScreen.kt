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

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientGeneralAssessmentScreen(
    patientId: Long,
    visitEpoch: Long,
    onNavigate: (String) -> Unit = {},
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val patient by sharedViewModel.getPatientById(patientId).collectAsState()

    var visitDate by remember { mutableLongStateOf(visitEpoch) }
    var generalHealth by remember { mutableStateOf<String?>(null) }
    var everOnDiet by remember { mutableStateOf<Boolean?>(null) }
    var comments by remember { mutableStateOf("") }

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
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= endOfTodayMillis
        }
    }

    val visitState = rememberDatePickerState(initialSelectedDateMillis = visitDate, selectableDates = selectableUpToToday)
    val visitInteractionSource = remember { MutableInteractionSource() }
    var showVisitPicker by remember { mutableStateOf(false) }

    val isSaving by sharedViewModel.isSaving.collectAsState()
    var attemptedSubmit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sharedViewModel.events.collect { ev ->
            when (ev) {
                is UiEvent.ShowMessage -> Toast.makeText(context, ev.msg, Toast.LENGTH_SHORT).show()
                is UiEvent.Navigate -> onNavigate(ev.route)
            }
        }
    }

    LaunchedEffect(visitInteractionSource) {
        visitInteractionSource.interactions.collect { it ->
            if (it is PressInteraction.Release) showVisitPicker = true
        }
    }

    val isVisitValid by derivedStateOf { visitDate <= endOfTodayMillis }
    val isGeneralHealthValid by derivedStateOf { !generalHealth.isNullOrBlank() }
    val isEverOnDietValid by derivedStateOf { everOnDiet != null }
    val isCommentsValid by derivedStateOf { comments.isNotBlank() }

    val isFormValid by derivedStateOf {
        isVisitValid && isGeneralHealthValid && isEverOnDietValid && isCommentsValid
    }

    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val healthOptions = listOf("Good", "Poor")

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("General Assessment") }) },
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
                            sharedViewModel.saveGeneralAssessment(
                                patientLocalId = patientId,
                                visitDateEpoch = visitDate,
                                generalHealth = generalHealth!!,
                                everOnDiet = everOnDiet!!,
                                comments = comments
                            )
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
                    value = DateUtils.toDisplayDate(visitDate),
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select visit date", color = hintColor) },
                    trailingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Visit date") },
                    interactionSource = visitInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                if (attemptedSubmit && !isVisitValid) {
                    Text("Visit date cannot be in the future", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }

                if (showVisitPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showVisitPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selected = visitState.selectedDateMillis
                                if (selected != null && selected <= endOfTodayMillis) {
                                    visitDate = DateUtils.normalizeToMidnight(selected)
                                } else if (selected != null) {
                                    Toast.makeText(context, "Cannot select a future date", Toast.LENGTH_SHORT).show()
                                }
                                showVisitPicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showVisitPicker = false }) { Text("Cancel") } }
                    ) {
                        DatePicker(state = visitState)
                    }
                }
            }

            Column {
                Text("General health *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    healthOptions.forEach { option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                            RadioButton(selected = generalHealth == option, onClick = { generalHealth = option })
                            Spacer(Modifier.width(4.dp))
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (attemptedSubmit && !isGeneralHealthValid) {
                    Text("Choose general health", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }

            Column {
                Text("Have you ever been on a diet to lose weight? *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        RadioButton(selected = everOnDiet == true, onClick = { everOnDiet = true })
                        Spacer(Modifier.width(4.dp))
                        Text("Yes", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = everOnDiet == false, onClick = { everOnDiet = false })
                        Spacer(Modifier.width(4.dp))
                        Text("No", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (attemptedSubmit && !isEverOnDietValid) {
                    Text("Please select Yes or No", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }

            Column {
                Text("Comments *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    placeholder = { Text("Drop your comments here", color = hintColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
                )

                if (attemptedSubmit && !isCommentsValid) {
                    Text("Comments are required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}
