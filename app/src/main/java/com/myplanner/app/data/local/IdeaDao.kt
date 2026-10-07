package com.myplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas WHERE archived = 0 ORDER BY pinned DESC, updatedAtEpochMillis DESC")
    fun observeActive(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas ORDER BY pinned DESC, updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): IdeaEntity?

    @Query("SELECT * FROM ideas WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<IdeaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(idea: IdeaEntity): Long

    @Update
    suspend fun update(idea: IdeaEntity)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE ideas SET pinned = :pinned, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, updatedAt: Long)

    @Query("UPDATE ideas SET archived = :archived, status = :status, updatedAtEpochMillis = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean, status: String, updatedAt: Long)

    @Query("SELECT * FROM ideas WHERE title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR status LIKE '%' || :query || '%' ORDER BY pinned DESC, updatedAtEpochMillis DESC LIMIT 50")
    fun search(query: String): Flow<List<IdeaEntity>>

    @Query("DELETE FROM ideas")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM ideas")
    suspend fun count(): Int

    @Query("SELECT * FROM ideas")
    suspend fun getAll(): List<IdeaEntity>
}
