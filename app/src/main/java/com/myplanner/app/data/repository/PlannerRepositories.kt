package com.myplanner.app.data.repository

import android.content.Context
import com.myplanner.app.data.local.IdeaDao
import com.myplanner.app.data.local.IdeaEntity
import com.myplanner.app.data.local.NoteDao
import com.myplanner.app.data.local.NoteEntity
import com.myplanner.app.data.local.ReminderDao
import com.myplanner.app.data.local.ReminderEntity
import com.myplanner.app.data.local.TaskDao
import com.myplanner.app.data.local.TaskEntity
import com.myplanner.app.data.local.VoiceNoteDao
import com.myplanner.app.data.local.VoiceNoteEntity
import com.myplanner.app.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()
    fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)
    suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun createTask(
        title: String,
        notes: String = "",
        dueAtEpochMillis: Long? = null,
        priority: Int = 0
    ): Long {
        val now = System.currentTimeMillis()
        return taskDao.insert(
            TaskEntity(
                title = title.trim(),
                notes = notes.trim(),
                dueAtEpochMillis = dueAtEpochMillis,
                priority = priority.coerceIn(0, 3),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.update(
            task.copy(
                title = task.title.trim(),
                notes = task.notes.trim(),
                priority = task.priority.coerceIn(0, 3),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val now = System.currentTimeMillis()
        taskDao.setCompleted(
            id = id,
            completed = completed,
            completedAt = if (completed) now else null,
            updatedAt = now
        )
    }

    suspend fun deleteTask(id: Long) {
        taskDao.deleteById(id)
    }
}

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val scheduler: ReminderScheduler
) {
    fun observeReminders(): Flow<List<ReminderEntity>> = reminderDao.observeAll()
    fun observeReminder(id: Long): Flow<ReminderEntity?> = reminderDao.observeById(id)
    suspend fun getReminder(id: Long): ReminderEntity? = reminderDao.getById(id)
    suspend fun getActiveScheduled(): List<ReminderEntity> = reminderDao.getActiveScheduled()

    suspend fun createReminder(
        title: String,
        notes: String = "",
        triggerAtEpochMillis: Long? = null,
        repeatType: String = ReminderEntity.REPEAT_NONE
    ): Long {
        val now = System.currentTimeMillis()
        val id = reminderDao.insert(
            ReminderEntity(
                title = title.trim(),
                notes = notes.trim(),
                triggerAtEpochMillis = triggerAtEpochMillis,
                repeatType = normalizeRepeat(repeatType),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
        val saved = reminderDao.getById(id)
        if (saved != null && saved.isActive) scheduler.schedule(saved)
        return id
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        val updated = reminder.copy(
            title = reminder.title.trim(),
            notes = reminder.notes.trim(),
            repeatType = normalizeRepeat(reminder.repeatType),
            updatedAtEpochMillis = System.currentTimeMillis()
        )
        reminderDao.update(updated)
        scheduler.cancel(updated.id)
        if (updated.isActive) scheduler.schedule(updated)
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        val now = System.currentTimeMillis()
        reminderDao.setCompleted(
            id = id,
            completed = completed,
            completedAt = if (completed) now else null,
            updatedAt = now
        )
        if (completed) scheduler.cancel(id)
        else reminderDao.getById(id)?.let { if (it.isActive) scheduler.schedule(it) }
    }

    suspend fun cancelReminder(id: Long) {
        reminderDao.cancel(id, System.currentTimeMillis())
        scheduler.cancel(id)
    }

    suspend fun deleteReminder(id: Long) {
        scheduler.cancel(id)
        reminderDao.deleteById(id)
    }

    suspend fun advanceRecurrence(id: Long): Boolean {
        val current = reminderDao.getById(id) ?: return false
        val trigger = current.triggerAtEpochMillis ?: return false
        if (current.repeatType == ReminderEntity.REPEAT_NONE) {
            reminderDao.setCompleted(id, true, System.currentTimeMillis(), System.currentTimeMillis())
            scheduler.cancel(id)
            return false
        }
        val next = nextOccurrence(trigger, current.repeatType) ?: return false
        val now = System.currentTimeMillis()
        reminderDao.updateTrigger(id, next, now)
        val updated = reminderDao.getById(id) ?: return false
        scheduler.schedule(updated)
        return true
    }

    suspend fun rescheduleAllActive() {
        scheduler.rescheduleAll(reminderDao.getActiveScheduled())
    }

    private fun normalizeRepeat(value: String): String = when (value.lowercase()) {
        ReminderEntity.REPEAT_DAILY, ReminderEntity.REPEAT_WEEKLY, ReminderEntity.REPEAT_MONTHLY -> value.lowercase()
        else -> ReminderEntity.REPEAT_NONE
    }

    companion object {
        fun nextOccurrence(fromMillis: Long, repeatType: String): Long? {
            val zone = java.time.ZoneId.systemDefault()
            val zdt = java.time.Instant.ofEpochMilli(fromMillis).atZone(zone)
            val next = when (repeatType) {
                ReminderEntity.REPEAT_DAILY -> zdt.plusDays(1)
                ReminderEntity.REPEAT_WEEKLY -> zdt.plusWeeks(1)
                ReminderEntity.REPEAT_MONTHLY -> zdt.plusMonths(1)
                else -> return null
            }
            return next.toInstant().toEpochMilli()
        }

        fun create(context: Context, dao: ReminderDao): ReminderRepository =
            ReminderRepository(dao, ReminderScheduler(context.applicationContext))
    }
}

class NoteRepository(private val noteDao: NoteDao) {
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeAll()

    suspend fun createNote(title: String, body: String = ""): Long {
        val now = System.currentTimeMillis()
        return noteDao.insert(
            NoteEntity(
                title = title.trim().ifBlank { body.trim().lineSequence().firstOrNull().orEmpty() },
                body = body.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
    }
}

class IdeaRepository(private val ideaDao: IdeaDao) {
    fun observeIdeas(): Flow<List<IdeaEntity>> = ideaDao.observeAll()

    suspend fun createIdea(title: String, body: String = ""): Long =
        ideaDao.insert(IdeaEntity(title = title.trim(), body = body.trim()))
}

class VoiceNoteRepository(private val voiceNoteDao: VoiceNoteDao) {
    fun observeVoiceNotes(): Flow<List<VoiceNoteEntity>> = voiceNoteDao.observeAll()

    suspend fun createVoiceNote(
        title: String,
        filePath: String = "",
        durationMillis: Long = 0
    ): Long = voiceNoteDao.insert(
        VoiceNoteEntity(title = title.trim(), filePath = filePath, durationMillis = durationMillis)
    )
}
