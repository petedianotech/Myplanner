package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local reminder with optional recurrence.
 * repeatType: none | daily | weekly | monthly
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val triggerAtEpochMillis: Long? = null,
    val repeatType: String = REPEAT_NONE,
    val completed: Boolean = false,
    val cancelled: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val REPEAT_NONE = "none"
        const val REPEAT_DAILY = "daily"
        const val REPEAT_WEEKLY = "weekly"
        const val REPEAT_MONTHLY = "monthly"
    }

    val isActive: Boolean
        get() = !completed && !cancelled && triggerAtEpochMillis != null
}
