package com.okellosoftwarez.patients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.okellosoftwarez.patients.viewmodel.SharedViewModel
import com.okellosoftwarez.patients.viewmodel.UiEvent
import com.okellosoftwarez.patients.ui.screens.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        sharedViewModel.events.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }

                is UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.msg)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "patients",
        builder = {

            composable("patients") {
                AllPatientsScreen(
                    onNavigate = { route ->
                        navController.navigate(route)
                    },
                    sharedViewModel = sharedViewModel
                )
            }

            composable("register") {
                PatientRegistrationScreen(
                    onNavigate = { route ->
                        navController.navigate(route)
                    },
                    sharedViewModel = sharedViewModel
                )
            }

            composable(
                route = "vitals/{patientId}",
                arguments = listOf(navArgument("patientId") { type = NavType.LongType })
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getLong("patientId") ?: 0L

                PatientVitalsScreen(
                    patientId = pid,
                    onNavigate = { route -> navController.navigate(route) },
                    sharedViewModel = sharedViewModel
                )
            }

            composable(
                route = "general/{patientId}/{visitEpoch}",
                arguments = listOf(
                    navArgument("patientId") { type = NavType.LongType },
                    navArgument("visitEpoch") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getLong("patientId") ?: 0L
                val epoch =
                    backStackEntry.arguments?.getLong("visitEpoch") ?: System.currentTimeMillis()
                PatientGeneralAssessmentScreen(
                    patientId = pid,
                    visitEpoch = epoch,
                    onNavigate = { route -> navController.navigate(route) },
                    sharedViewModel = sharedViewModel
                )
            }

            composable(
                route = "overweight/{patientId}/{visitEpoch}",
                arguments = listOf(
                    navArgument("patientId") { type = NavType.LongType },
                    navArgument("visitEpoch") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val pid = backStackEntry.arguments?.getLong("patientId") ?: 0L
                val epoch =
                    backStackEntry.arguments?.getLong("visitEpoch") ?: System.currentTimeMillis()
                PatientOverweightAssessmentScreen(
                    patientId = pid,
                    visitEpoch = epoch,
                    onNavigate = { route -> navController.navigate(route) },
                    sharedViewModel = sharedViewModel
                )
            }
        }
    )
}

