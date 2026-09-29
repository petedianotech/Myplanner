package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["triggerAtEpochMillis"]),
        Index(value = ["completed", "cancelled", "triggerAtEpochMillis"]),
        Index(value = ["title"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val triggerAtEpochMillis: Long? = null,
    val repeatType: String = REPEAT_NONE,
    val completed: Boolean = false,
    val cancelled: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val lastFiredAtEpochMillis: Long? = null,
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

    fun isMissed(now: Long = System.currentTimeMillis()): Boolean {
        val at = triggerAtEpochMillis ?: return false
        return !completed && !cancelled && at < now
    }
}
