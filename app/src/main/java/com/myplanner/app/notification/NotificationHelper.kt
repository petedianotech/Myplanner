package com.myplanner.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myplanner.app.MainActivity
import com.myplanner.app.R
import java.util.concurrent.atomic.AtomicBoolean

object NotificationHelper {

    const val CHANNEL_REMINDERS = "reminders"
    private const val CHANNEL_NAME = "Reminders"
    private const val CHANNEL_DESC =
        "Local alerts for scheduled reminders. Delivered on this device only."
    private const val TAG = "PeteNotify"

    private val channelsReady = AtomicBoolean(false)

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (channelsReady.get()) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
        channelsReady.set(true)
    }

    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        notes: String = "",
        isMissed: Boolean = false,
        vibrate: Boolean = true
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

        val snooze10 = snoozePending(context, reminderId, ReminderScheduler.SNOOZE_MINUTES, 2)
        val snooze60 = snoozePending(context, reminderId, ReminderScheduler.SNOOZE_60_MINUTES, 3)

        val body = when {
            notes.isNotBlank() -> notes
            isMissed -> "Missed · Mark done or snooze from here"
            else -> "Done · Snooze 10m · Snooze 1h"
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
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .apply {
                if (vibrate) setVibrate(longArrayOf(0, 40, 40, 40))
                else setVibrate(longArrayOf(0))
            }
            .setContentIntent(openPending)
            .addAction(0, "Done", donePending)
            .addAction(0, "Snooze 10m", snooze10)
            .addAction(0, "Snooze 1h", snooze60)
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) {
            Log.w(TAG, "Notifications disabled — grant POST_NOTIFICATIONS in system settings")
        }
        try {
            nm.notify(notificationId(reminderId), notification)
            Log.i(TAG, "Posted reminder notification id=$reminderId title=$contentTitle")
        } catch (se: SecurityException) {
            Log.e(TAG, "POST_NOTIFICATIONS permission missing", se)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to post notification", t)
        }
    }

    private fun snoozePending(
        context: Context,
        reminderId: Long,
        minutes: Long,
        codeOffset: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            ReminderScheduler.requestCode(reminderId) + codeOffset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(reminderId))
    }

    fun notificationId(reminderId: Long): Int =
        ReminderScheduler.requestCode(reminderId)
}
