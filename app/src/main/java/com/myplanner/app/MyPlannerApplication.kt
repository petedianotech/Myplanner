package com.myplanner.app

import android.app.Application

/**
 * Application entry point.
 * Phase 1: lightweight initialization only.
 * Future: Room database, WorkManager configuration, and reminder scheduling setup.
 */
class MyPlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Intentionally minimal — no background services or heavy init in Phase 1.
    }
}
