package com.myplanner.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.myplanner.app.MainActivity
import com.myplanner.app.R
import com.myplanner.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
            onUpdate(context, mgr, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.myplanner.app.widget.REFRESH"

        fun requestUpdate(context: Context) {
            context.sendBroadcast(Intent(context, TodayWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            })
        }

        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val views = RemoteViews(context.packageName, R.layout.widget_today)
                val openApp = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openApp)

                try {
                    val db = AppDatabase.getInstance(context)
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone)
                    val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
                    val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                    val now = System.currentTimeMillis()

                    val tasks = db.taskDao().getAll()
                    val reminders = db.reminderDao().getActiveScheduled() + db.reminderDao().getMissed(now)

                    val openTasks = tasks.count { t ->
                        !t.completed && (
                            t.dueAtEpochMillis == null ||
                                t.dueAtEpochMillis in start until end ||
                                (t.dueAtEpochMillis != null && t.dueAtEpochMillis < now)
                            )
                    }
                    val openReminders = reminders.distinctBy { it.id }.count { r ->
                        !r.completed && !r.cancelled && r.triggerAtEpochMillis != null &&
                            (r.triggerAtEpochMillis in start until end || r.triggerAtEpochMillis < now)
                    }
                    val nextReminder = reminders
                        .filter { !it.completed && !it.cancelled && it.triggerAtEpochMillis != null && it.triggerAtEpochMillis >= now }
                        .minByOrNull { it.triggerAtEpochMillis!! }

                    val dateLabel = today.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
                    views.setTextViewText(R.id.widget_date, dateLabel)
                    views.setTextViewText(
                        R.id.widget_summary,
                        when {
                            openTasks == 0 && openReminders == 0 -> "All clear today"
                            else -> buildString {
                                if (openTasks > 0) append("$openTasks task${if (openTasks == 1) "" else "s"}")
                                if (openTasks > 0 && openReminders > 0) append(" · ")
                                if (openReminders > 0) append("$openReminders reminder${if (openReminders == 1) "" else "s"}")
                            }
                        }
                    )
                    if (nextReminder != null) {
                        val zdt = java.time.Instant.ofEpochMilli(nextReminder.triggerAtEpochMillis!!)
                            .atZone(zone)
                        val time = "%02d:%02d".format(zdt.hour, zdt.minute)
                        views.setTextViewText(R.id.widget_next, "Next: ${nextReminder.title} · $time")
                    } else {
                        views.setTextViewText(R.id.widget_next, "No upcoming reminders")
                    }
                } catch (_: Exception) {
                    views.setTextViewText(R.id.widget_date, "MyPlanner")
                    views.setTextViewText(R.id.widget_summary, "Open the app")
                    views.setTextViewText(R.id.widget_next, "")
                }

                manager.updateAppWidget(widgetId, views)
            }
        }
    }
}
