package com.myplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks
        ORDER BY completed ASC,
                 CASE WHEN dueAtEpochMillis IS NULL THEN 1 ELSE 0 END,
                 dueAtEpochMillis ASC,
                 priority DESC,
                 createdAtEpochMillis DESC
        """)
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("""
        UPDATE tasks SET completed = :completed,
            completedAtEpochMillis = :completedAt,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :id
        """)
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?, updatedAt: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT * FROM tasks
        WHERE dueAtEpochMillis IS NOT NULL
          AND dueAtEpochMillis >= :startInclusive
          AND dueAtEpochMillis < :endExclusive
        ORDER BY dueAtEpochMillis ASC, priority DESC
        """)
    fun observeDueBetween(startInclusive: Long, endExclusive: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE title LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY completed ASC, updatedAtEpochMillis DESC
        LIMIT 50
        """)
    fun search(query: String): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 0")
    suspend fun countOpen(): Int

    @Query("""
        SELECT * FROM tasks
        WHERE completed = 1 AND completedAtEpochMillis IS NOT NULL
        ORDER BY completedAtEpochMillis DESC
        LIMIT 200
        """)
    suspend fun getRecentlyCompleted(): List<TaskEntity>
}
