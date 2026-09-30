package com.myplanner.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myplanner.app.data.local.AppDatabase
import com.myplanner.app.data.repository.IdeaRepository
import com.myplanner.app.data.repository.NoteRepository
import com.myplanner.app.data.repository.ReminderRepository
import com.myplanner.app.data.repository.TaskRepository
import com.myplanner.app.data.repository.VoiceNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class PlanKind { TASK, REMINDER }

enum class HomeFilter { ALL, TODAY, OVERDUE, UPCOMING }

data class PlanItem(
    val id: Long,
    val kind: PlanKind,
    val title: String,
    val atMillis: Long?,
    val completed: Boolean,
    val priority: Int = 0
) {
    fun isOverdue(now: Long): Boolean =
        !completed && atMillis != null && atMillis < now
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayTasksRemaining: Int = 0,
    val todayRemindersScheduled: Int = 0,
    val overdueCount: Int = 0,
    val completedTodayCount: Int = 0,
    val todayItems: List<PlanItem> = emptyList(),
    val upcomingItems: List<PlanItem> = emptyList(),
    val filter: HomeFilter = HomeFilter.ALL,
    val filteredItems: List<PlanItem> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val taskRepository = TaskRepository(db.taskDao())
    val reminderRepository = ReminderRepository.create(application, db.reminderDao())
    val noteRepository = NoteRepository(db.noteDao())
    val ideaRepository = IdeaRepository(db.ideaDao())
    val voiceNoteRepository = VoiceNoteRepository.create(application, db.voiceNoteDao())

    private val filterFlow = MutableStateFlow(HomeFilter.ALL)

    fun setFilter(filter: HomeFilter) {
        filterFlow.value = filter
    }

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepository.observeTasks(),
        reminderRepository.observeReminders(),
        filterFlow
    ) { tasks, reminders, filter ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfTomorrow = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()

        val taskItems = tasks.map {
            PlanItem(it.id, PlanKind.TASK, it.title, it.dueAtEpochMillis, it.completed, it.priority)
        }
        val reminderItems = reminders.asSequence()
            .filter { !it.cancelled }
            .map { PlanItem(it.id, PlanKind.REMINDER, it.title, it.triggerAtEpochMillis, it.completed) }
            .toList()
        val all = taskItems + reminderItems

        val todayItems = all.filter { item ->
            val at = item.atMillis
            when {
                at == null && !item.completed && item.kind == PlanKind.TASK -> true
                at == null -> false
                at in startOfToday until startOfTomorrow -> true
                item.isOverdue(now) && at < startOfToday -> true
                else -> false
            }
        }.sortedWith(
            compareBy<PlanItem> { it.completed }
                .thenByDescending { it.priority }
                .thenBy { it.atMillis ?: Long.MAX_VALUE }
        )

        val upcoming = all.asSequence()
            .filter { item ->
                val at = item.atMillis ?: return@filter false
                !item.completed && at >= startOfTomorrow
            }
            .sortedBy { it.atMillis }
            .take(8)
            .toList()

        val filtered = when (filter) {
            HomeFilter.ALL -> todayItems + upcoming.filter { up ->
                todayItems.none { it.id == up.id && it.kind == up.kind }
            }
            HomeFilter.TODAY -> todayItems.filter {
                val at = it.atMillis
                at == null || at in startOfToday until startOfTomorrow || it.isOverdue(now)
            }
            HomeFilter.OVERDUE -> all.filter { it.isOverdue(now) }
                .sortedBy { it.atMillis }
            HomeFilter.UPCOMING -> upcoming
        }

        HomeUiState(
            isLoading = false,
            todayTasksRemaining = todayItems.count { it.kind == PlanKind.TASK && !it.completed },
            todayRemindersScheduled = todayItems.count { it.kind == PlanKind.REMINDER && !it.completed },
            overdueCount = all.count { it.isOverdue(now) },
            completedTodayCount = todayItems.count { it.completed },
            todayItems = todayItems,
            upcomingItems = upcoming,
            filter = filter,
            filteredItems = filtered
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun toggleItem(item: PlanItem) {
        viewModelScope.launch {
            when (item.kind) {
                PlanKind.TASK -> taskRepository.setCompleted(item.id, !item.completed)
                PlanKind.REMINDER -> reminderRepository.setCompleted(item.id, !item.completed)
            }
        }
    }

    fun snoozeReminder(item: PlanItem, minutes: Long) {
        if (item.kind != PlanKind.REMINDER) return
        viewModelScope.launch {
            reminderRepository.snoozeMinutes(item.id, minutes.coerceAtLeast(1L))
        }
    }
}
