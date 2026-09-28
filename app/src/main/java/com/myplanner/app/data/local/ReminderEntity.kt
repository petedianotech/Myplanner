package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Minimal local reminder model for first-run and future reminder features.
 * Scheduling (AlarmManager) is intentionally not implemented in this phase.
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val triggerAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
