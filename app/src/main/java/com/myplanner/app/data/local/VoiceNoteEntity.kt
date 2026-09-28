package com.myplanner.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String = "",
    val durationMillis: Long = 0,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
