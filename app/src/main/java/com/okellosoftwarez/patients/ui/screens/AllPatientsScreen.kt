package com.okellosoftwarez.patients.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.okellosoftwarez.patients.data.dao.PatientWithLastVitals
import com.okellosoftwarez.patients.util.BmiUtils
import com.okellosoftwarez.patients.util.DateUtils
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.okellosoftwarez.patients.viewmodel.SharedViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPatientsScreen(
    onNavigate: (String) -> Unit = {},
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
//    var isSyncing by remember { mutableStateOf(false) }
    // LiveData -> Compose state
    val patients by sharedViewModel.patientsWithLastVitals.observeAsState(emptyList())
    val pendingCount by sharedViewModel.pendingSyncCount.observeAsState(0)
    val isSyncing by sharedViewModel.isSyncing.collectAsState()

    // filter state (epoch at midnight)
    var filterDateEpoch by remember { mutableStateOf<Long?>(null) }

    // --- Material3 DatePicker state for filter dialog ---
    val endOfTodayMillis = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        cal.timeInMillis
    }
    val selectableUpToToday = remember(endOfTodayMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= endOfTodayMillis
        }
    }
    val filterState = rememberDatePickerState(initialSelectedDateMillis = filterDateEpoch, selectableDates = selectableUpToToday)
    var showFilterPicker by remember { mutableStateOf(false) }

    // small, clickable textfield interaction
    val filterInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(filterInteractionSource) {
        filterInteractionSource.interactions.collect { it ->
            if (it is PressInteraction.Release) showFilterPicker = true
        }
    }

    val filteredList = remember(patients, filterDateEpoch) {
        if (filterDateEpoch == null) patients
        else patients.filter { it.lastVisitDateEpoch == filterDateEpoch }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("IntelliSoft Patients") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Register Patient") },
                icon = { Icon(Icons.Default.Person, contentDescription = "Register Client") },
                onClick = { onNavigate("register") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation()
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
//            SyncNowScreen()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    sharedViewModel.requestSync()
//                                    Toast
//                                        .makeText(context, "$pendingCount pending sync(s)", Toast.LENGTH_SHORT)
//                                        .show()
                                }
                                .weight(1f)
//                            .padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Pending syncs",
                                tint = if (pendingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$pendingCount pending sync(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }



                    Spacer(Modifier.width(8.dp))
                    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    OutlinedTextField(
                        value = filterDateEpoch?.let { DateUtils.toDisplayDate(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = true,
                        placeholder = { Text("Filter by date", color = hintColor, style = MaterialTheme.typography.bodySmall) },
                        trailingIcon = {
                            if (filterDateEpoch != null) {
                                IconButton(onClick = { filterDateEpoch = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear filter")
                                }
                            } else {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                            }
                        },
                        interactionSource = filterInteractionSource,
                        modifier = Modifier
                            .width(160.dp)
                            .height(44.dp)
                            .weight(1f)
                        ,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (showFilterPicker) {
                DatePickerDialog(
                    onDismissRequest = { showFilterPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val selected = filterState.selectedDateMillis
                            if (selected != null) filterDateEpoch = DateUtils.normalizeToMidnight(selected)
                            showFilterPicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFilterPicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = filterState)
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (filterDateEpoch == null) "No patients registered." else "No records for selected date.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredList, key = { it.patient.id }) { entry ->
                        PatientCard(entry) {
                            onNavigate("vitals/${entry.patient.id}")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun PatientCard(entry: PatientWithLastVitals, onClick: () -> Unit) {
    val patient = entry.patient
    val lastBmi = entry.lastBmi
    val status = lastBmi?.let { BmiUtils.getBmiStatus(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            val initials = "${patient.firstName.firstOrNull()?.uppercaseChar() ?: ' '}${patient.lastName.firstOrNull()?.uppercaseChar() ?: ' '}"
            val avatarColors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(avatarColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("${patient.firstName} ${patient.lastName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                val ageText = DateUtils.calculateAge(patient.dobEpoch)?.let { "$it years old" } ?: "-"
                Text(ageText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                entry.lastVisitDateEpoch?.let {
                    Spacer(Modifier.height(8.dp))
                    Text("Last visit was on ${DateUtils.toDisplayDate(it)}", style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp
                    ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic)
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                if (lastBmi != null && status != null) {
                    val (bg, contentColor) = when (status) {
                        "Underweight" -> Pair(Color(0xFFFFA726), Color.White)
                        "Normal" -> Pair(Color(0xFF66BB6A), Color.White)
                        else -> Pair(Color(0xFFEF5350), Color.White)
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = bg,
                        tonalElevation = 2.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(status, color = contentColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("No vitals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
