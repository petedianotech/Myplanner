package com.myplanner.app.data

import com.myplanner.app.data.local.TaskEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Computes consecutive calendar-day streak from completed tasks.
 */
object StreakCalculator {

    fun currentStreak(completed: List<TaskEntity>): Int {
        val zone = ZoneId.systemDefault()
        val days = completed
            .mapNotNull { it.completedAtEpochMillis }
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .toSet()
        if (days.isEmpty()) return 0

        var streak = 0
        var cursor = LocalDate.now()
        // Allow today or yesterday as the anchor
        if (cursor !in days) {
            cursor = cursor.minusDays(1)
            if (cursor !in days) return 0
        }
        while (cursor in days) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun completedToday(completed: List<TaskEntity>): Int {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        return completed.count { t ->
            t.completedAtEpochMillis?.let {
                Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today
            } == true
        }
    }
}
