package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits",
    indices = [Index(value = ["title"]), Index(value = ["archived"])]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String = "✅",
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDay: String? = null, // yyyy-MM-dd
    val archived: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
