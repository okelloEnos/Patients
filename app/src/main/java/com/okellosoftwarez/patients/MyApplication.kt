package com.okellosoftwarez.patients

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * MyApplication
 *
 * The entry point for the application where Hilt dependency injection
 * is initialized. All other app components can now use @AndroidEntryPoint
 * for dependency injection (e.g., Activities, ViewModels, Workers, etc.).
 */
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Perform any global initialization here if needed
        // e.g., initializing Timber logging, analytics, etc.
    }
}
