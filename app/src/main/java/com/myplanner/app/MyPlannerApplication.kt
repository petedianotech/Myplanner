package com.myplanner.app

import android.app.Application
import com.myplanner.app.notification.MissedReminderRecovery
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
            runCatching { MissedReminderRecovery.run(this@MyPlannerApplication) }
        }
    }
}
