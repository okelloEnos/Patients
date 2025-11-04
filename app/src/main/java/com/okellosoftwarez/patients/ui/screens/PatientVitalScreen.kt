package com.okellosoftwarez.patients.ui.screens

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.ViewModel

@Composable
fun PatientVitalScreen(
    patientId: Long,
    onNavigate: (String) -> Unit,
//    sharedViewModel: SharedViewModel = viewModel()
){
    var height by remember { mutableStateOf(0.0) }
    var weight by remember { mutableStateOf(0.0) }
    var heightStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Vitals for patient #patientId")
        OutlinedTextField(
            value = heightStr,
            onValueChange = {
                heightStr = it
                height = it.toDoubleOrNull() ?: 0.0
            },
            label = { Text("Height (cm)") }
        )

        OutlinedTextField(
            value = weightStr,
            onValueChange = {
                weightStr = it
                weight = it.toDoubleOrNull() ?: 0.0
            },
            label = { Text("Weight (kg)") }
        )

//        val bmi =  if (height > 0 && weight > 0) BmiUtils.calculateBmi(weight, height) else 0.0
//        Text(text = "BMI: ${"%.2f".format(bmi)} (${BmiUtils.bmiCategory(bmi)})")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if(height <= 0 || weight <= 0) return@Button
            val visitDate = System.currentTimeMillis()
//            sharedViewModel.saveVitalsAndNavigate(patientId, visitDate, height, weight)
        }) {
            Text("Save Vitals")
        }
    }
}