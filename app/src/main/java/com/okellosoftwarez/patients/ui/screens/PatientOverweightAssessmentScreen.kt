package com.okellosoftwarez.patients.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PatientOverweightAssessmentScreen(
    patientId: Long,
    visitEpoch: Long,
    onNavigate: (String) -> Unit,
//    sharedViewModel: SharedViewModel = viewModel()
){
    var generalHealth by remember { mutableStateOf("Good") }
    var usingDrugs by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Overweight Assessment")
        Text("Visit date: ${visitEpoch}")
        OutlinedTextField(
            value = generalHealth,
            onValueChange = {
                generalHealth = it
            },
            label = { Text("General Health (Good/Poor)") }
        )
        Row {
            Button(onClick = { usingDrugs = true }) { Text("Using drugs: Yes") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { usingDrugs = false }) { Text("No") }
        }
        OutlinedTextField(
            value = comments,
            onValueChange = {
                comments = it
            },
            label = { Text("Comments") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if(generalHealth.isBlank() || comments.isBlank()) return@Button
//            sharedViewModel.saveOverweightAssessment(patientId, visitEpoch, generalHealth, usingDrugs, comments)
        }) { Text("Save Overweight Assessment") }
    }
}