package com.myplanner.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myplanner.app.MainActivity
import com.myplanner.app.R
import java.util.concurrent.atomic.AtomicBoolean

object NotificationHelper {

    const val CHANNEL_REMINDERS = "reminders_v2"
    const val CHANNEL_LIVE = "pete_live_call"
    private const val CHANNEL_NAME = "Reminders"
    private const val CHANNEL_DESC =
        "Local alerts for scheduled reminders. Delivered on this device only."

    private val channelsReady = AtomicBoolean(false)

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        if (channelsReady.get()) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.deleteNotificationChannel("reminders") }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 150, 250)
            enableLights(true)
            setShowBadge(true)
            setSound(soundUri, audioAttrs)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setAllowBubbles(true)
            }
        }
        manager.createNotificationChannel(channel)

        val liveChannel = NotificationChannel(
            CHANNEL_LIVE,
            "Pete Live Call",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ongoing Live voice call with Pete. Stays visible while you switch apps."
            enableVibration(false)
            setShowBadge(false)
            setSound(null, null)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(liveChannel)
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
            data = android.net.Uri.parse("petediano://reminder/done/$reminderId")
        }
        val donePending = PendingIntent.getBroadcast(
            context,
            ReminderScheduler.requestCode(reminderId) + 1,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snooze10Intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, ReminderScheduler.SNOOZE_MINUTES)
            data = android.net.Uri.parse("petediano://reminder/snooze10/$reminderId")
        }
        val snooze10Pending = PendingIntent.getBroadcast(
            context,
            ReminderScheduler.requestCode(reminderId) + 2,
            snooze10Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snooze60Intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_SNOOZE_MINUTES, ReminderScheduler.SNOOZE_60_MINUTES)
            data = android.net.Uri.parse("petediano://reminder/snooze60/$reminderId")
        }
        val snooze60Pending = PendingIntent.getBroadcast(
            context,
            ReminderScheduler.requestCode(reminderId) + 3,
            snooze60Intent,
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
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setOngoing(false)
            .apply {
                if (vibrate) setVibrate(longArrayOf(0, 250, 150, 250))
                else setVibrate(longArrayOf(0))
            }
            .setContentIntent(openPending)
            .addAction(0, "Done", donePending)
            .addAction(0, "Snooze 10m", snooze10Pending)
            .addAction(0, "Snooze 1h", snooze60Pending)
            .build()

        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) {
            android.util.Log.w("PeteNotify", "Notifications disabled for package — enable in system settings")
        }
        try {
            nm.notify(notificationId(reminderId), notification)
            android.util.Log.i("PeteNotify", "Posted reminder notification id=$reminderId missed=$isMissed")
        } catch (se: SecurityException) {
            android.util.Log.e("PeteNotify", "POST_NOTIFICATIONS missing", se)
        } catch (t: Throwable) {
            android.util.Log.e("PeteNotify", "Failed to post notification", t)
        }
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(reminderId))
    }

    fun notificationId(reminderId: Long): Int =
        ReminderScheduler.requestCode(reminderId)
}
