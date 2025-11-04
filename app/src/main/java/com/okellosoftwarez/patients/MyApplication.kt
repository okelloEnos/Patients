package com.okellosoftwarez.patients

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * MyApplication
 *
 * Initializes Hilt (via @HiltAndroidApp) and wires Hilt's WorkerFactory into WorkManager
 * so that Workers can receive @Inject dependencies (e.g. @HiltWorker + @AssistedInject).
 */
@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    // Hilt will provide the worker factory
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // any other app-wide initialisation can go here (e.g., Timber)
    }

    /**
     * Provide WorkManager configuration that uses Hilt's WorkerFactory.
     * This is required for Hilt-injected workers (HiltWorker).
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}

