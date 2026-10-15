package com.myplanner.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.local.ReminderEntity
import com.myplanner.app.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles alarm fire, Done, and Snooze without requiring the UI process.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1L)
        if (id < 0) {
            Log.w(TAG, "Missing reminder id action=$action")
            return
        }

        Log.i(TAG, "onReceive action=$action id=$id")
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        scope.launch {
            try {
                val db = AppDatabase.getInstance(appContext)
                val repo = ReminderRepository.create(appContext, db.reminderDao())
                when (action) {
                    ReminderScheduler.ACTION_FIRE -> handleFire(appContext, repo, id)
                    ReminderScheduler.ACTION_DONE -> handleDone(appContext, repo, id)
                    ReminderScheduler.ACTION_SNOOZE -> {
                        val minutes = intent.getLongExtra(
                            ReminderScheduler.EXTRA_SNOOZE_MINUTES,
                            ReminderScheduler.SNOOZE_MINUTES
                        )
                        handleSnooze(appContext, repo, id, minutes)
                    }
                    else -> Log.w(TAG, "Unknown action=$action")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Receiver failed action=$action id=$id", t)
            } finally {
                try {
                    pendingResult.finish()
                } catch (_: Throwable) {
                }
            }
        }
    }

    private suspend fun handleFire(
        context: Context,
        repo: ReminderRepository,
        id: Long
    ) {
        val reminder = repo.getReminder(id) ?: run {
            Log.w(TAG, "Fire: reminder $id not found")
            return
        }
        if (reminder.completed || reminder.cancelled) {
            Log.i(TAG, "Fire: reminder $id inactive")
            return
        }

        val trigger = reminder.triggerAtEpochMillis ?: return
        val lastFired = reminder.lastFiredAtEpochMillis
        // Dedup only if we already fired this exact trigger within the last 2 minutes
        if (lastFired != null && lastFired >= trigger && System.currentTimeMillis() - lastFired < 120_000L) {
            Log.i(TAG, "Fire: already fired recently id=$id")
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
        Log.i(TAG, "Fire: posted notification id=$id missed=$isMissed")

        if (reminder.repeatType != ReminderEntity.REPEAT_NONE) {
            repo.advanceRecurrence(id)
        }
    }

    private suspend fun handleDone(
        context: Context,
        repo: ReminderRepository,
        id: Long
    ) {
        repo.setCompleted(id, true)
        NotificationHelper.cancelNotification(context, id)
        Log.i(TAG, "Done id=$id")
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
        Log.i(TAG, "Snooze id=$id minutes=$minutes")
    }

    companion object {
        private const val TAG = "PeteReminderRx"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
