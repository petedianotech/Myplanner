package com.myplanner.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database foundation for offline-first architecture.
 * Phase 1: schema marker only — entities and DAOs will be added when features arrive.
 * This establishes the single source of truth pattern for user-created data.
 */
@Database(
    entities = [SchemaMeta::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private const val DATABASE_NAME = "myplanner.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
