package com.myplanner.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Restores reminder alarms after device boot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                ReminderRepository.create(context, db.reminderDao()).rescheduleAllActive()
            } finally {
                pending.finish()
            }
        }
    }
}
