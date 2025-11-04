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
fun PatientGeneralAssessmentScreen(
    patientId: Long,
    visitEpoch: Long,
    onNavigate: (String) -> Unit,
//    sharedViewModel: SharedViewModel = viewModel()
){
    var generalHealth by remember { mutableStateOf("Good") }
    var everOnDiet by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf("") }

    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text("General Assessment")
        Text("Visit Date: ${visitEpoch}")
        OutlinedTextField(
            value = generalHealth,
            onValueChange = {
                generalHealth = it
            },
            label = { Text("General Health (Good/Poor)") }
        )
        Row {
            Button(onClick = { everOnDiet = true }) { Text("Ever on diet: Yes") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { everOnDiet = false }) { Text("No") }
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
//            sharedViewModel.saveGeneralAssessment(patientId, visitEpoch, generalHealth, everOnDiet, comments)
        }) { Text("Save General Assessment") }

    }
}