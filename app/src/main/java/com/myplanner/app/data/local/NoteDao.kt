package com.myplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("""SELECT * FROM notes WHERE archived = 0 ORDER BY pinned DESC, updatedAtEpochMillis DESC""")
    fun observeActive(): Flow<List<NoteEntity>>
    @Query("""SELECT * FROM notes WHERE archived = 1 ORDER BY updatedAtEpochMillis DESC""")
    fun observeArchived(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<NoteEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long
    @Update
    suspend fun update(note: NoteEntity)
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
    @Query("""UPDATE notes SET pinned = :pinned, updatedAtEpochMillis = :updatedAt WHERE id = :id""")
    suspend fun setPinned(id: Long, pinned: Boolean, updatedAt: Long)
    @Query("""UPDATE notes SET archived = :archived, updatedAtEpochMillis = :updatedAt WHERE id = :id""")
    suspend fun setArchived(id: Long, archived: Boolean, updatedAt: Long)
    @Query("""SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY pinned DESC, updatedAtEpochMillis DESC LIMIT 50""")
    fun search(query: String): Flow<List<NoteEntity>>
    @Query("DELETE FROM notes")
    suspend fun deleteAll()
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun count(): Int
}
