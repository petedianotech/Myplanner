package com.myplanner.app

import android.app.Application
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.repository.ReminderRepository
import com.myplanner.app.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyPlannerApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        appScope.launch {
            val db = AppDatabase.getInstance(this@MyPlannerApplication)
            ReminderRepository.create(this@MyPlannerApplication, db.reminderDao())
                .rescheduleAllActive()
        }
    }
}
