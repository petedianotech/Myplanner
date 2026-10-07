package com.myplanner.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("""SELECT * FROM reminders ORDER BY completed ASC, cancelled ASC, CASE WHEN triggerAtEpochMillis IS NULL THEN 1 ELSE 0 END, triggerAtEpochMillis ASC, createdAtEpochMillis DESC""")
    fun observeAll(): Flow<List<ReminderEntity>>
    @Query("""SELECT * FROM reminders WHERE completed = 0 AND cancelled = 0 AND triggerAtEpochMillis IS NOT NULL""")
    suspend fun getActiveScheduled(): List<ReminderEntity>
    @Query("""SELECT * FROM reminders WHERE completed = 0 AND cancelled = 0 AND triggerAtEpochMillis IS NOT NULL AND triggerAtEpochMillis < :now ORDER BY triggerAtEpochMillis ASC""")
    fun observeMissed(now: Long): Flow<List<ReminderEntity>>
    @Query("""SELECT * FROM reminders WHERE completed = 0 AND cancelled = 0 AND triggerAtEpochMillis IS NOT NULL AND triggerAtEpochMillis < :now ORDER BY triggerAtEpochMillis ASC""")
    suspend fun getMissed(now: Long): List<ReminderEntity>
    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?
    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ReminderEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long
    @Update
    suspend fun update(reminder: ReminderEntity)
    @Query("""UPDATE reminders SET completed = :completed, completedAtEpochMillis = :completedAt, updatedAtEpochMillis = :updatedAt WHERE id = :id""")
    suspend fun setCompleted(id: Long, completed: Boolean, completedAt: Long?, updatedAt: Long)
    @Query("""UPDATE reminders SET cancelled = 1, updatedAtEpochMillis = :updatedAt WHERE id = :id""")
    suspend fun cancel(id: Long, updatedAt: Long)
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
    @Query("""UPDATE reminders SET triggerAtEpochMillis = :triggerAt, updatedAtEpochMillis = :updatedAt WHERE id = :id""")
    suspend fun updateTrigger(id: Long, triggerAt: Long, updatedAt: Long)
    @Query("""UPDATE reminders SET lastFiredAtEpochMillis = :firedAt, updatedAtEpochMillis = :updatedAt WHERE id = :id""")
    suspend fun markFired(id: Long, firedAt: Long, updatedAt: Long)
    @Query("""SELECT * FROM reminders WHERE triggerAtEpochMillis IS NOT NULL AND triggerAtEpochMillis >= :startInclusive AND triggerAtEpochMillis < :endExclusive AND cancelled = 0 ORDER BY triggerAtEpochMillis ASC""")
    fun observeTriggerBetween(startInclusive: Long, endExclusive: Long): Flow<List<ReminderEntity>>
    @Query("""SELECT * FROM reminders WHERE cancelled = 0 AND (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') ORDER BY completed ASC, triggerAtEpochMillis ASC LIMIT 50""")
    fun search(query: String): Flow<List<ReminderEntity>>
    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun count(): Int

    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<ReminderEntity>
}
