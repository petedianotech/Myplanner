package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueAtEpochMillis: Long? = null,
    val completed: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
