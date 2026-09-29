package com.myplanner.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.myplanner.app.data.local.ReminderEntity

/**
 * Schedules exact local alarms for reminders via AlarmManager.
 * Cancels and reschedules cleanly so edits/deletes never leave orphan alarms.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        val trigger = reminder.triggerAtEpochMillis ?: return
        if (!reminder.isActive) {
            cancel(reminder.id)
            return
        }
        if (trigger <= System.currentTimeMillis() &&
            reminder.repeatType == ReminderEntity.REPEAT_NONE
        ) {
            return
        }

        val pending = pendingIntent(reminder.id, reminder.title)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    requestCode(reminder.id),
                    Intent(context, Class.forName("com.myplanner.app.MainActivity")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_REMINDER_ID, reminder.id)
                    },
                    pendingFlags()
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(trigger, showIntent),
                    pending
                )
            } else {
                @Suppress("DEPRECATION")
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger, pending)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        }
    }

    fun cancel(reminderId: Long) {
        val pending = pendingIntent(reminderId, title = "")
        alarmManager.cancel(pending)
        pending.cancel()
    }

    fun rescheduleAll(reminders: List<ReminderEntity>) {
        reminders.forEach { schedule(it) }
    }

    private fun pendingIntent(reminderId: Long, title: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            intent,
            pendingFlags()
        )
    }

    private fun pendingFlags(): Int {
        return PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }

    companion object {
        const val ACTION_FIRE = "com.myplanner.app.action.REMINDER_FIRE"
        const val ACTION_DONE = "com.myplanner.app.action.REMINDER_DONE"
        const val ACTION_SNOOZE = "com.myplanner.app.action.REMINDER_SNOOZE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val SNOOZE_MINUTES = 10L
        const val SNOOZE_30_MINUTES = 30L

        fun requestCode(reminderId: Long): Int {
            return (reminderId xor (reminderId ushr 32)).toInt()
        }
    }
}
