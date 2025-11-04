package com.okellosoftwarez.patients.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun PatientRegistrationScreen(
    onNavigate: (String) -> Unit,
//    sharedViewModel: SharedViewModel = viewModel()
) {
    var patientId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var today = System.currentTimeMillis()


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)){
        Text("Register Patient")
        OutlinedTextField(
            value = patientId,
            onValueChange = { patientId = it },
            label = { Text("Patient ID (unique)") }
        )
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") }
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { gender = "Male" }) { Text("Male") }
            Button(onClick = { gender = "Female" }) { Text("Female") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            if(patientId.isBlank() || firstName.isBlank() || lastName.isBlank()) return@Button
//            sharedViewModel.registerPatient(patientId, today, firstName, lastName, null, gender)
        }) {
            Text("Save and continue to Vitals")
        }
    }

}