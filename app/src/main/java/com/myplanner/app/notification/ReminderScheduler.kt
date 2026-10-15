package com.myplanner.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.myplanner.app.MainActivity
import com.myplanner.app.data.local.ReminderEntity

/**
 * Exact local alarms for reminders via AlarmManager.
 *
 * Uses setAlarmClock (most reliable across OEM battery savers) with
 * setExactAndAllowWhileIdle / setAndAllowWhileIdle fallbacks.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: ReminderEntity) {
        val trigger = reminder.triggerAtEpochMillis ?: run {
            cancel(reminder.id)
            return
        }
        if (!reminder.isActive) {
            cancel(reminder.id)
            return
        }

        val now = System.currentTimeMillis()
        // One-shot already in the past by more than 30s → leave to missed recovery, don't schedule
        if (trigger <= now - 30_000L && reminder.repeatType == ReminderEntity.REPEAT_NONE) {
            Log.i(TAG, "Skip schedule past one-shot id=${reminder.id} trigger=$trigger")
            return
        }
        // Slightly past or "now" → schedule a few seconds out so the alarm still fires
        val effectiveTrigger = if (trigger <= now) now + 2_000L else trigger

        val pending = firePendingIntent(reminder.id, reminder.title)
        try {
            scheduleExact(effectiveTrigger, pending, reminder.id)
            Log.i(TAG, "Scheduled id=${reminder.id} at $effectiveTrigger (${reminder.title})")
        } catch (se: SecurityException) {
            Log.e(TAG, "Exact alarm blocked for id=${reminder.id}", se)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        effectiveTrigger,
                        pending
                    )
                } else {
                    @Suppress("DEPRECATION")
                    alarmManager.set(AlarmManager.RTC_WAKEUP, effectiveTrigger, pending)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Fallback schedule failed id=${reminder.id}", t)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Schedule failed id=${reminder.id}", t)
        }
    }

    private fun scheduleExact(triggerAt: Long, pending: PendingIntent, reminderId: Long) {
        // setAlarmClock is the most reliable path (shows in status bar, survives Doze)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val showIntent = PendingIntent.getActivity(
                context,
                showRequestCode(reminderId),
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_REMINDER_ID, reminderId)
                },
                pendingFlags()
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, showIntent),
                pending
            )
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            return
        }
        @Suppress("DEPRECATION")
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    fun cancel(reminderId: Long) {
        try {
            val pending = firePendingIntent(reminderId, title = "")
            alarmManager.cancel(pending)
            pending.cancel()
        } catch (t: Throwable) {
            Log.w(TAG, "Cancel failed id=$reminderId", t)
        }
    }

    fun rescheduleAll(reminders: List<ReminderEntity>) {
        reminders.forEach { schedule(it) }
    }

    /** Whether this device currently allows exact alarms (Android 12+). */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else null
    }

    private fun firePendingIntent(reminderId: Long, title: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            // Unique data so PendingIntents don't collide across reminders
            data = android.net.Uri.parse("petediano://reminder/$reminderId")
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            intent,
            pendingFlags()
        )
    }

    private fun pendingFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    companion object {
        private const val TAG = "PeteAlarm"

        const val ACTION_FIRE = "com.myplanner.app.action.REMINDER_FIRE"
        const val ACTION_DONE = "com.myplanner.app.action.REMINDER_DONE"
        const val ACTION_SNOOZE = "com.myplanner.app.action.REMINDER_SNOOZE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val SNOOZE_MINUTES = 10L
        const val SNOOZE_30_MINUTES = 30L
        const val SNOOZE_60_MINUTES = 60L

        fun requestCode(reminderId: Long): Int =
            (reminderId xor (reminderId ushr 32)).toInt() and 0x7FFFFFFF

        fun showRequestCode(reminderId: Long): Int =
            (requestCode(reminderId) + 0x10000) and 0x7FFFFFFF
    }
}
