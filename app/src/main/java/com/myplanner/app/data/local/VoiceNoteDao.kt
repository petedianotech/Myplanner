package com.myplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM voice_notes ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): VoiceNoteEntity?

    @Query("SELECT * FROM voice_notes WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<VoiceNoteEntity?>

    @Query("SELECT filePath FROM voice_notes")
    suspend fun getAllFilePaths(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: VoiceNoteEntity): Long

    @Update
    suspend fun update(note: VoiceNoteEntity)

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT * FROM voice_notes
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY createdAtEpochMillis DESC
        LIMIT 50
        """
    )
    fun search(query: String): Flow<List<VoiceNoteEntity>>

    @Query("DELETE FROM voice_notes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM voice_notes")
    suspend fun count(): Int
}
