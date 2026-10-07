package com.myplanner.app.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.myplanner.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports all planner data to a shareable JSON file.
 * Real working backup — not a placeholder.
 */
object BackupExporter {

    suspend fun exportAndShare(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val db = AppDatabase.getInstance(context)
            val root = JSONObject()
            root.put("app", "petediano")
            root.put("version", 1)
            root.put(
                "exportedAt",
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date())
            )

            val tasks = JSONArray()
            db.taskDao().getAll().forEach { t ->
                tasks.put(
                    JSONObject()
                        .put("id", t.id)
                        .put("title", t.title)
                        .put("notes", t.notes)
                        .put("dueAt", t.dueAtEpochMillis)
                        .put("priority", t.priority)
                        .put("completed", t.completed)
                        .put("completedAt", t.completedAtEpochMillis)
                        .put("createdAt", t.createdAtEpochMillis)
                )
            }
            root.put("tasks", tasks)

            val reminders = JSONArray()
            db.reminderDao().getAll().forEach { r ->
                reminders.put(
                    JSONObject()
                        .put("id", r.id)
                        .put("title", r.title)
                        .put("notes", r.notes)
                        .put("triggerAt", r.triggerAtEpochMillis)
                        .put("completed", r.completed)
                        .put("createdAt", r.createdAtEpochMillis)
                )
            }
            root.put("reminders", reminders)

            val notes = JSONArray()
            db.noteDao().getAll().forEach { n ->
                notes.put(
                    JSONObject()
                        .put("id", n.id)
                        .put("title", n.title)
                        .put("body", n.body)
                        .put("createdAt", n.createdAtEpochMillis)
                        .put("updatedAt", n.updatedAtEpochMillis)
                )
            }
            root.put("notes", notes)

            val ideas = JSONArray()
            db.ideaDao().getAll().forEach { i ->
                ideas.put(
                    JSONObject()
                        .put("id", i.id)
                        .put("title", i.title)
                        .put("body", i.body)
                        .put("category", i.category)
                        .put("createdAt", i.createdAtEpochMillis)
                )
            }
            root.put("ideas", ideas)

            val habits = JSONArray()
            db.habitDao().getAll().forEach { h ->
                habits.put(
                    JSONObject()
                        .put("id", h.id)
                        .put("title", h.title)
                        .put("emoji", h.emoji)
                        .put("streak", h.streak)
                        .put("bestStreak", h.bestStreak)
                        .put("lastCompletedDay", h.lastCompletedDay)
                        .put("archived", h.archived)
                )
            }
            root.put("habits", habits)

            val dir = File(context.cacheDir, "backups").apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "petediano_backup_$stamp.json")
            file.writeText(root.toString(2))

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "petediano backup $stamp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(share, "Share backup").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
