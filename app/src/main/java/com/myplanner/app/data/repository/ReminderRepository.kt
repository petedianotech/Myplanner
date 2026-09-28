package com.myplanner.app.data.repository

import com.myplanner.app.data.local.ReminderDao
import com.myplanner.app.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    fun observeReminders(): Flow<List<ReminderEntity>> = reminderDao.observeAll()

    suspend fun createReminder(
        title: String,
        notes: String = "",
        triggerAtEpochMillis: Long? = null
    ): Long {
        return reminderDao.insert(
            ReminderEntity(
                title = title.trim(),
                notes = notes.trim(),
                triggerAtEpochMillis = triggerAtEpochMillis
            )
        )
    }
}
