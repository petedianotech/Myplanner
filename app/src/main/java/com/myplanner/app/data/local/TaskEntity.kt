package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Personal task — offline source of truth.
 * priority: 0=none, 1=low, 2=medium, 3=high
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueAtEpochMillis: Long? = null,
    val priority: Int = 0,
    val completed: Boolean = false,
    val completedAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
