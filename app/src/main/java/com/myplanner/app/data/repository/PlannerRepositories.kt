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
import com.myplanner.app.data.voice.VoiceAudioStorage
import com.myplanner.app.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

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
    fun observeMissed(now: Long = System.currentTimeMillis()): Flow<List<ReminderEntity>> =
        reminderDao.observeMissed(now)

    suspend fun getReminder(id: Long): ReminderEntity? = reminderDao.getById(id)
    suspend fun getActiveScheduled(): List<ReminderEntity> = reminderDao.getActiveScheduled()
    suspend fun getMissed(now: Long = System.currentTimeMillis()): List<ReminderEntity> =
        reminderDao.getMissed(now)

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
        reminderDao.getById(id)?.let { if (it.isActive) scheduler.schedule(it) }
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

    suspend fun snooze(id: Long, newTriggerAt: Long) {
        val current = reminderDao.getById(id) ?: return
        if (current.completed || current.cancelled) return
        val updated = current.copy(
            triggerAtEpochMillis = newTriggerAt,
            completed = false,
            cancelled = false,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
        reminderDao.update(updated)
        scheduler.cancel(id)
        scheduler.schedule(updated)
    }

    suspend fun snoozeMinutes(id: Long, minutes: Long) {
        snooze(id, System.currentTimeMillis() + minutes * 60_000L)
    }

    suspend fun snoozeTonight(id: Long) {
        val zone = ZoneId.systemDefault()
        val tonight = LocalDate.now(zone).atTime(LocalTime.of(20, 0)).atZone(zone)
        var target = tonight.toInstant().toEpochMilli()
        if (target <= System.currentTimeMillis()) {
            target = tonight.plusDays(1).toInstant().toEpochMilli()
        }
        snooze(id, target)
    }

    suspend fun markFired(id: Long) {
        val now = System.currentTimeMillis()
        reminderDao.markFired(id, now, now)
    }

    suspend fun advanceRecurrence(id: Long): Boolean {
        val current = reminderDao.getById(id) ?: return false
        val trigger = current.triggerAtEpochMillis ?: return false
        if (current.repeatType == ReminderEntity.REPEAT_NONE) return false
        val next = nextOccurrence(trigger, current.repeatType) ?: return false
        val now = System.currentTimeMillis()
        reminderDao.updateTrigger(id, next, now)
        reminderDao.getById(id)?.let { scheduler.schedule(it) }
        return true
    }

    suspend fun rescheduleAllActive() {
        scheduler.rescheduleAll(reminderDao.getActiveScheduled())
    }

    private fun normalizeRepeat(value: String): String = when (value.lowercase()) {
        ReminderEntity.REPEAT_DAILY,
        ReminderEntity.REPEAT_WEEKLY,
        ReminderEntity.REPEAT_MONTHLY -> value.lowercase()
        else -> ReminderEntity.REPEAT_NONE
    }

    companion object {
        fun nextOccurrence(fromMillis: Long, repeatType: String): Long? {
            val zone = ZoneId.systemDefault()
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
    fun observeActive(): Flow<List<NoteEntity>> = noteDao.observeActive()
    fun observeArchived(): Flow<List<NoteEntity>> = noteDao.observeArchived()
    fun observeAll(): Flow<List<NoteEntity>> = noteDao.observeAll()
    fun observeNotes(): Flow<List<NoteEntity>> = noteDao.observeActive()
    fun observeNote(id: Long): Flow<NoteEntity?> = noteDao.observeById(id)
    suspend fun getNote(id: Long): NoteEntity? = noteDao.getById(id)

    suspend fun createNote(title: String = "", body: String = ""): Long {
        val now = System.currentTimeMillis()
        val trimmedTitle = title.trim().ifBlank {
            body.trim().lineSequence().firstOrNull()?.take(80).orEmpty()
        }
        return noteDao.insert(
            NoteEntity(
                title = trimmedTitle,
                body = body.trim(),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
    }

    suspend fun updateNote(note: NoteEntity) {
        noteDao.update(
            note.copy(
                title = note.title.trim(),
                body = note.body.trim(),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun setPinned(id: Long, pinned: Boolean) {
        noteDao.setPinned(id, pinned, System.currentTimeMillis())
    }

    suspend fun setArchived(id: Long, archived: Boolean) {
        noteDao.setArchived(id, archived, System.currentTimeMillis())
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteById(id)
    }
}

class IdeaRepository(private val ideaDao: IdeaDao) {
    fun observeActive(): Flow<List<IdeaEntity>> = ideaDao.observeActive()
    fun observeArchived(): Flow<List<IdeaEntity>> = ideaDao.observeArchived()
    fun observeAll(): Flow<List<IdeaEntity>> = ideaDao.observeAll()
    fun observeIdeas(): Flow<List<IdeaEntity>> = ideaDao.observeActive()
    fun observeIdea(id: Long): Flow<IdeaEntity?> = ideaDao.observeById(id)
    suspend fun getIdea(id: Long): IdeaEntity? = ideaDao.getById(id)

    suspend fun createIdea(
        title: String,
        body: String = "",
        category: String = IdeaEntity.CATEGORY_OTHER,
        status: String = IdeaEntity.STATUS_NEW
    ): Long {
        val now = System.currentTimeMillis()
        return ideaDao.insert(
            IdeaEntity(
                title = title.trim(),
                body = body.trim(),
                category = normalizeCategory(category),
                status = normalizeStatus(status),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
    }

    suspend fun updateIdea(idea: IdeaEntity) {
        ideaDao.update(
            idea.copy(
                title = idea.title.trim(),
                body = idea.body.trim(),
                category = normalizeCategory(idea.category),
                status = normalizeStatus(idea.status),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun setPinned(id: Long, pinned: Boolean) {
        ideaDao.setPinned(id, pinned, System.currentTimeMillis())
    }

    suspend fun setArchived(id: Long, archived: Boolean) {
        val status = if (archived) IdeaEntity.STATUS_ARCHIVED else IdeaEntity.STATUS_NEW
        ideaDao.setArchived(id, archived, status, System.currentTimeMillis())
    }

    suspend fun deleteIdea(id: Long) {
        ideaDao.deleteById(id)
    }

    private fun normalizeCategory(value: String): String = when (value.lowercase()) {
        IdeaEntity.CATEGORY_APP,
        IdeaEntity.CATEGORY_BUSINESS,
        IdeaEntity.CATEGORY_CONTENT,
        IdeaEntity.CATEGORY_SCHOOL,
        IdeaEntity.CATEGORY_PERSONAL -> value.lowercase()
        else -> IdeaEntity.CATEGORY_OTHER
    }

    private fun normalizeStatus(value: String): String = when (value.lowercase()) {
        IdeaEntity.STATUS_WORKING,
        IdeaEntity.STATUS_COMPLETED,
        IdeaEntity.STATUS_ARCHIVED -> value.lowercase()
        else -> IdeaEntity.STATUS_NEW
    }
}

class VoiceNoteRepository(
    private val voiceNoteDao: VoiceNoteDao,
    private val storage: VoiceAudioStorage
) {
    fun observeVoiceNotes(): Flow<List<VoiceNoteEntity>> = voiceNoteDao.observeAll()
    fun observeVoiceNote(id: Long): Flow<VoiceNoteEntity?> = voiceNoteDao.observeById(id)
    suspend fun getVoiceNote(id: Long): VoiceNoteEntity? = voiceNoteDao.getById(id)

    suspend fun createVoiceNote(
        title: String,
        filePath: String = "",
        durationMillis: Long = 0,
        description: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        return voiceNoteDao.insert(
            VoiceNoteEntity(
                title = title.trim().ifBlank { defaultTitle(now) },
                description = description.trim(),
                filePath = filePath,
                durationMillis = durationMillis.coerceAtLeast(0L),
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now
            )
        )
    }

    suspend fun updateVoiceNote(note: VoiceNoteEntity) {
        voiceNoteDao.update(
            note.copy(
                title = note.title.trim().ifBlank { defaultTitle(note.createdAtEpochMillis) },
                description = note.description.trim(),
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun rename(id: Long, title: String) {
        val existing = voiceNoteDao.getById(id) ?: return
        updateVoiceNote(existing.copy(title = title))
    }

    suspend fun deleteVoiceNote(id: Long) {
        val existing = voiceNoteDao.getById(id)
        voiceNoteDao.deleteById(id)
        storage.deleteIfExists(existing?.filePath)
        storage.cleanupOrphans(voiceNoteDao.getAllFilePaths())
    }

    companion object {
        fun defaultTitle(atMillis: Long = System.currentTimeMillis()): String {
            val zdt = java.time.Instant.ofEpochMilli(atMillis)
                .atZone(java.time.ZoneId.systemDefault())
            return "Voice Note · %02d/%02d %02d:%02d".format(
                zdt.monthValue, zdt.dayOfMonth, zdt.hour, zdt.minute
            )
        }

        fun create(context: Context, dao: VoiceNoteDao): VoiceNoteRepository =
            VoiceNoteRepository(dao, VoiceAudioStorage(context))
    }
}
