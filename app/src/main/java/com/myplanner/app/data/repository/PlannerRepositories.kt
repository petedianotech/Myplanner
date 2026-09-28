package com.myplanner.app.data.repository

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
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    fun observeReminders(): Flow<List<ReminderEntity>> = reminderDao.observeAll()

    suspend fun createReminder(
        title: String,
        notes: String = "",
        triggerAtEpochMillis: Long? = null
    ): Long = reminderDao.insert(
        ReminderEntity(
            title = title.trim(),
            notes = notes.trim(),
            triggerAtEpochMillis = triggerAtEpochMillis
        )
    )

    suspend fun setCompleted(id: Long, completed: Boolean) {
        reminderDao.setCompleted(id, completed)
    }
}

class TaskRepository(private val taskDao: TaskDao) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

    suspend fun createTask(
        title: String,
        notes: String = "",
        dueAtEpochMillis: Long? = null
    ): Long = taskDao.insert(
        TaskEntity(
            title = title.trim(),
            notes = notes.trim(),
            dueAtEpochMillis = dueAtEpochMillis
        )
    )

    suspend fun setCompleted(id: Long, completed: Boolean) {
        taskDao.setCompleted(id, completed)
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
        VoiceNoteEntity(
            title = title.trim(),
            filePath = filePath,
            durationMillis = durationMillis
        )
    )
}
