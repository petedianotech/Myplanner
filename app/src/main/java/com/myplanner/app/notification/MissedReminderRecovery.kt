package com.myplanner.app.notification

import android.content.Context
import android.util.Log
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.repository.ReminderRepository

/**
 * On app start / boot: show notifications for missed one-shot reminders
 * and reschedule all still-active future alarms.
 */
object MissedReminderRecovery {

    private const val TAG = "PeteMissed"

    suspend fun run(context: Context) {
        val app = context.applicationContext
        val db = AppDatabase.getInstance(app)
        val repo = ReminderRepository.create(app, db.reminderDao())
        val now = System.currentTimeMillis()

        // Reschedule future alarms first
        repo.rescheduleAllActive()

        // Notify for overdue one-shots that never got a notification
        val missed = repo.getMissed(now)
        Log.i(TAG, "Missed count=${missed.size}")
        for (r in missed) {
            if (r.completed || r.cancelled) continue
            val lastFired = r.lastFiredAtEpochMillis
            val trigger = r.triggerAtEpochMillis ?: continue
            // Skip if we already notified after this trigger
            if (lastFired != null && lastFired >= trigger) continue
            try {
                repo.markFired(r.id)
                NotificationHelper.showReminderNotification(
                    context = app,
                    reminderId = r.id,
                    title = r.title,
                    notes = r.notes,
                    isMissed = true
                )
                Log.i(TAG, "Notified missed id=${r.id}")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed missed notify id=${r.id}", t)
            }
        }
    }
}
