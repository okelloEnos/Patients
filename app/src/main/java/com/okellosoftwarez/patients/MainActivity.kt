package com.okellosoftwarez.patients

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.okellosoftwarez.patients.ui.theme.PatientsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        // Schedule periodic sync worker (example: every 15 minutes)
//        val work = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
//        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
//            "sync_worker",
//            ExistingPeriodicWorkPolicy.KEEP,
//            work
//        )
//
        enableEdgeToEdge()
        setContent {
            PatientsTheme {
                Surface {
                    AppNavHost()
                }
            }
        }
    }
}