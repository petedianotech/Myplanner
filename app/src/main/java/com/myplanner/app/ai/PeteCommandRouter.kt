package com.myplanner.app.ai

import com.myplanner.app.data.repository.NoteRepository
import com.myplanner.app.data.repository.ReminderRepository
import com.myplanner.app.data.repository.TaskRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max

class PeteCommandRouter(
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository,
    private val noteRepository: NoteRepository
) {
    data class Result(
        val reply: String,
        val navigateTo: String? = null,
        val focusMinutes: Int? = null
    )

    suspend fun handle(raw: String): Result {
        val text = raw.trim().lowercase(Locale.getDefault())
        if (text.isBlank()) return Result("I'm listening, boss.")

        if (text in listOf("hey", "hi", "hello", "hey pete", "hi pete", "hello pete")) {
            return Result("Hey boss. What do we need done?")
        }

        if (containsAny(text, "what's on", "what is on", "whats on", "today", "rundown", "brief", "schedule")) {
            return Result("Here's your brief for today, boss.", navigateTo = "brief")
        }

        focusMatch(text)?.let { mins ->
            return Result(
                "Focus mode for $mins minutes. I'll keep things quiet.",
                navigateTo = "focus",
                focusMinutes = mins
            )
        }

        parseAddTask(text)?.let { title ->
            taskRepository.createTask(title)
            return Result("Got it. Task added: $title")
        }

        parseReminder(text)?.let { (title, whenMillis) ->
            reminderRepository.createReminder(title = title, triggerAtEpochMillis = whenMillis)
            val whenLabel = formatRelative(whenMillis)
            return Result("Reminder set for $whenLabel: $title")
        }

        parseNote(text)?.let { body ->
            noteRepository.createNote(title = body.take(40), body = body)
            return Result("Noted: $body")
        }

        if (containsAny(text, "help", "what can you")) {
            return Result(
                "I can add tasks, set reminders, take notes, start focus timers, and open your daily brief. " +
                    "Or just chat — Gemini handles the rest when the key is set."
            )
        }

        return Result("I heard you. Try: add task buy milk, remind me in 1 hour call mom, or start focus 25.")
    }

    private fun focusMatch(text: String): Int? {
        if (!containsAny(text, "focus", "pomodoro", "deep work")) return null
        val m = Regex("""(\d+)\s*(m|min|mins|minute|minutes)""").find(text)
        if (m != null) return m.groupValues[1].toInt().coerceIn(5, 180)
        return 25
    }

    private fun parseAddTask(text: String): String? {
        val patterns = listOf(
            Regex("""^(?:add |new |create )?(?:a )?task\s+(.+)$"""),
            Regex("""^todo\s+(.+)$"""),
            Regex("""^i need to\s+(.+)$""")
        )
        for (p in patterns) {
            p.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.length > 1 }?.let {
                return it.replaceFirstChar { c -> c.uppercase() }
            }
        }
        return null
    }

    private fun parseReminder(text: String): Pair<String, Long>? {
        if (!containsAny(text, "remind", "reminder", "alert me")) return null
        var cleaned = text
        cleaned = cleaned.replace(Regex("""^hey pete[, ]*"""), "")
        cleaned = cleaned.replace(Regex("""remind me (to |about )?"""), "")
        cleaned = cleaned.replace(Regex("""^set (a )?reminder (to |for )?"""), "")
        cleaned = cleaned.trim()
        if (cleaned.length < 2) return null
        val (titlePart, whenMillis) = extractWhen(cleaned)
        val title = titlePart.trim().ifBlank { cleaned }.replaceFirstChar { it.uppercase() }
        return title to whenMillis
    }

    private fun parseNote(text: String): String? {
        val p = Regex("""^(?:take a note|note|remember that)\s+(.+)$""")
        return p.find(text)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.length > 1 }
    }

    private fun extractWhen(text: String): Pair<String, Long> {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        Regex("""\bin\s+(\d+)\s*(m|min|mins|minute|minutes)\b""").find(text)?.let { m ->
            val n = m.groupValues[1].toInt()
            val title = text.replace(m.value, "").trim()
            return title to System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(n.toLong())
        }
        Regex("""\bin\s+(\d+)\s*(h|hr|hour|hours)\b""").find(text)?.let { m ->
            val n = m.groupValues[1].toInt()
            val title = text.replace(m.value, "").trim()
            return title to System.currentTimeMillis() + TimeUnit.HOURS.toMillis(n.toLong())
        }
        if (text.contains("tomorrow")) {
            val title = text.replace("tomorrow", "").trim()
            val at = now.toLocalDate().plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
            return title to at
        }
        return text to System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
    }

    private fun formatRelative(epoch: Long): String {
        val delta = max(0, epoch - System.currentTimeMillis())
        val mins = TimeUnit.MILLISECONDS.toMinutes(delta).toInt()
        return when {
            mins < 60 -> "in ${mins}m"
            mins < 24 * 60 -> "in ${mins / 60}h"
            else -> "later"
        }
    }

    private fun containsAny(text: String, vararg keys: String) = keys.any { text.contains(it) }
}
