package com.myplanner.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.local.ReminderEntity
import com.myplanner.app.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles alarm fire, Done, and Snooze without requiring the UI process to be alive.
 * Missed one-shot reminders stay active (past trigger) until the user completes,
 * snoozes, or reschedules — no endless duplicate records.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        if (id < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val repo = ReminderRepository.create(context, db.reminderDao())
                when (intent.action) {
                    ReminderScheduler.ACTION_FIRE -> handleFire(context, repo, id)
                    ReminderScheduler.ACTION_DONE -> handleDone(context, repo, id)
                    ReminderScheduler.ACTION_SNOOZE -> {
                        val minutes = intent.getLongExtra(
                            ReminderScheduler.EXTRA_SNOOZE_MINUTES,
                            ReminderScheduler.SNOOZE_MINUTES
                        )
                        handleSnooze(context, repo, id, minutes)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleFire(
        context: Context,
        repo: ReminderRepository,
        id: Long
    ) {
        val reminder = repo.getReminder(id) ?: return
        if (reminder.completed || reminder.cancelled) return

        val lastFired = reminder.lastFiredAtEpochMillis
        val trigger = reminder.triggerAtEpochMillis ?: return
        if (lastFired != null && lastFired >= trigger) {
            return
        }

        repo.markFired(id)
        val isMissed = trigger < System.currentTimeMillis() - 60_000L
        NotificationHelper.showReminderNotification(
            context = context,
            reminderId = id,
            title = reminder.title,
            notes = reminder.notes,
            isMissed = isMissed
        )

        when (reminder.repeatType) {
            ReminderEntity.REPEAT_NONE -> {
            }
            else -> {
                repo.advanceRecurrence(id)
            }
        }
    }

    private suspend fun handleDone(
        context: Context,
        repo: ReminderRepository,
        id: Long
    ) {
        repo.setCompleted(id, true)
        NotificationHelper.cancelNotification(context, id)
    }

    private suspend fun handleSnooze(
        context: Context,
        repo: ReminderRepository,
        id: Long,
        minutes: Long
    ) {
        val reminder = repo.getReminder(id) ?: return
        if (reminder.completed || reminder.cancelled) return
        repo.snoozeMinutes(id, minutes.coerceAtLeast(1L))
        NotificationHelper.cancelNotification(context, id)
    }
}
