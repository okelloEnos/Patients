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
fun PatientRegistrationScreen(
    onNavigate: (String) -> Unit = {},
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var patientId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var gender by remember { mutableStateOf("Male") }

    var dobEpoch by remember { mutableStateOf<Long?>(null) }
    var registrationEpoch by remember { mutableLongStateOf(DateUtils.todayEpoch()) }

    val dobInteractionSource = remember { MutableInteractionSource() }
    val dorInteractionSource = remember { MutableInteractionSource() }

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

    val dobState = rememberDatePickerState(
        initialSelectedDateMillis = dobEpoch,
        selectableDates = selectableUpToToday
    )
    val regState = rememberDatePickerState(
        initialSelectedDateMillis = registrationEpoch,
        selectableDates = selectableUpToToday
    )

    var showDobPicker by remember { mutableStateOf(false) }
    var showDorPicker by remember { mutableStateOf(false) }

    val isSaving by sharedViewModel.isSaving.collectAsState()

    LaunchedEffect(Unit) {
        sharedViewModel.events.collect { ev ->
            when (ev) {
                is UiEvent.ShowMessage ->
                    Toast.makeText(context, ev.msg, Toast.LENGTH_SHORT).show()

                is UiEvent.Navigate ->
                    onNavigate(ev.route)
            }
        }
    }

    LaunchedEffect(dobInteractionSource) {
        dobInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDobPicker = true
            }
        }
    }

    LaunchedEffect(dorInteractionSource) {
        dorInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDorPicker = true
            }
        }
    }

    var attemptedSubmit by remember { mutableStateOf(false) }

    val todayEpoch = DateUtils.todayEpoch()
    val isFormValid by derivedStateOf {
        patientId.isNotBlank()
                && firstName.isNotBlank()
                && lastName.isNotBlank()
                && registrationEpoch <= todayEpoch
                && dobEpoch != null
                && gender.isNotBlank()
    }

    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Patient Registration") }) },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onNavigate("patients") },
                        shape = MaterialTheme.shapes.small.copy(
                            all = androidx.compose.foundation.shape.CornerSize(
                                12.dp
                            )
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                    ) {
                        Text(text = "Close", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            attemptedSubmit = true
                            if (!isFormValid) {
                                Toast.makeText(
                                    context,
                                    "Please complete all required fields.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            sharedViewModel.registerPatient(
                                patientId = patientId,
                                registrationDateEpoch = registrationEpoch,
                                firstName = firstName,
                                lastName = lastName,
                                dobEpoch = dobEpoch,
                                gender = gender
                            )
                        },
                        enabled = isFormValid && !isSaving,
                        shape = MaterialTheme.shapes.small.copy(
                            all = androidx.compose.foundation.shape.CornerSize(
                                12.dp
                            )
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        modifier = Modifier
                            .weight(3f)
                            .height(48.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text("Create Patient", style = MaterialTheme.typography.labelLarge)
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
            Column {
                Text("Patient ID *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = patientId,
                    onValueChange = { patientId = it },
                    placeholder = { Text("Enter patient ID", color = hintColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            Column {
                Text("First Name *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("Enter patient's first name", color = hintColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text("Last Name *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Enter patient's last name", color = hintColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text("Date of Birth *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = dobEpoch?.let { DateUtils.toDisplayDate(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select patient's date of birth", color = hintColor) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "DOB"
                        )
                    },
                    interactionSource = dobInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                if (attemptedSubmit && dobEpoch == null) {
                    Text(
                        text = "Date of birth is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (showDobPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDobPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selected = dobState.selectedDateMillis
                                if (selected != null && selected <= endOfTodayMillis) {
                                    dobEpoch = DateUtils.normalizeToMidnight(selected)
                                } else if (selected != null) {
                                    Toast.makeText(
                                        context,
                                        "Cannot select a future date",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                showDobPicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDobPicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = dobState)
                    }
                }
            }

            Column {
                Text("Gender *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = gender == "Male",
                            onClick = { gender = "Male" }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Male", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == "Female",
                            onClick = { gender = "Female" }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Female", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Column {
                Text("Registration Date *", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = DateUtils.toDisplayDate(registrationEpoch),
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select patient's registration date", color = hintColor) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Registration date"
                        )
                    },
                    interactionSource = dorInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                if (attemptedSubmit && registrationEpoch > todayEpoch) {
                    Text(
                        text = "Registration date cannot be in the future",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                if (showDorPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDorPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val selected = regState.selectedDateMillis
                                if (selected != null && selected <= endOfTodayMillis) {
                                    registrationEpoch = DateUtils.normalizeToMidnight(selected)
                                } else if (selected != null) {
                                    Toast.makeText(
                                        context,
                                        "Cannot select a future date",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                showDorPicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDorPicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = regState)
                    }
                }
            }
        }
    }
}
