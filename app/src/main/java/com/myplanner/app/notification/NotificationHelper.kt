package com.myplanner.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myplanner.app.MainActivity
import com.myplanner.app.R

object NotificationHelper {

    const val CHANNEL_REMINDERS = "reminders"
    private const val CHANNEL_NAME = "Reminders"
    private const val CHANNEL_DESC =
        "Local alerts for scheduled reminders. Delivered on this device only."

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        notes: String = "",
        isMissed: Boolean = false
    ) {
        ensureChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        val openPending = PendingIntent.getActivity(
            context,
            ReminderScheduler.requestCode(reminderId),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_DONE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
        }
        val donePending = PendingIntent.getBroadcast(
            context,
            ReminderScheduler.requestCode(reminderId) + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, ReminderScheduler.SNOOZE_MINUTES)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            ReminderScheduler.requestCode(reminderId) + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = when {
            notes.isNotBlank() -> notes
            isMissed -> "Missed · Tap to open, mark done, or snooze"
            else -> "Tap to open · Done or snooze from here"
        }
        val contentTitle = if (isMissed) {
            "Missed: ${title.ifBlank { "Reminder" }}"
        } else {
            title.ifBlank { "Reminder" }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(contentTitle)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openPending)
            .addAction(0, "Done", donePending)
            .addAction(0, "Snooze ${ReminderScheduler.SNOOZE_MINUTES}m", snoozePending)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(notificationId(reminderId), notification)
        } catch (_: SecurityException) {
        }
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(reminderId))
    }

    fun notificationId(reminderId: Long): Int =
        ReminderScheduler.requestCode(reminderId)
}
