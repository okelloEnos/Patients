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

/**
 * AppNavHost - central navigation graph
 *
 * Manages all screens:
 *  - Registration
 *  - Vitals
 *  - General Assessment
 *  - Overweight Assessment
 *  - Patient List
 *
 * Uses Hilt for ViewModel injection.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val vm: SharedViewModel = hiltViewModel()

//    // Observe VM events to handle navigation and UI messages
//    LaunchedEffect(Unit) {
//        vm.events.collect { event ->
//            when (event) {
//                is UiEvent.Navigate -> navController.navigate(event.route)
//                is UiEvent.ShowMessage -> {
//                    // Optional: You can connect this to a Snackbar host or Toast
//                    println("UI Message: ${event.msg}")
//                }
//            }
//        }
//    }

    NavHost(
        navController = navController,
        startDestination = "register"
    ) {
        composable("register") {
            PatientRegistrationScreen(
                onNavigate = { route -> navController.navigate(route) },
//                vm = vm
            )
        }

        composable(
            route = "vitals/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val pid = backStackEntry.arguments?.getLong("patientId") ?: 0L
            PatientRegistrationScreen(
//                patientId = pid,
                onNavigate = { route -> navController.navigate(route) },
//                vm = vm
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
            val vEpoch = backStackEntry.arguments?.getLong("visitEpoch") ?: System.currentTimeMillis()
            PatientRegistrationScreen(
//                patientId = pid,
//                visitEpoch = vEpoch,
                onNavigate = { route -> navController.navigate(route) },
//                vm = vm
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
            val vEpoch = backStackEntry.arguments?.getLong("visitEpoch") ?: System.currentTimeMillis()
            PatientRegistrationScreen(
//                patientId = pid,
//                visitEpoch = vEpoch,
                onNavigate = { route -> navController.navigate(route) },
//                vm = vm
            )
        }

        composable("patients") {
            PatientListScreen(
                onNavigate = { route -> navController.navigate(route) },
//                shareViewModel = vm
            )
        }
    }
}
