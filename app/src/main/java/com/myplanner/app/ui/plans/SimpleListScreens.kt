package com.myplanner.app.ui.plans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.ReminderEntity
import com.myplanner.app.data.local.TaskEntity
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppIconButton
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.formatPlanTime
import com.myplanner.app.ui.theme.Spacing
import com.myplanner.app.ui.theme.Warning
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class PlanFilter { ALL, TODAY, UPCOMING, OVERDUE, COMPLETED }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlansScreen(
    onOpenTask: (Long) -> Unit,
    onOpenReminder: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val tasks by viewModel.taskRepository.observeTasks().collectAsStateWithLifecycle(emptyList())
    val reminders by viewModel.reminderRepository.observeReminders().collectAsStateWithLifecycle(emptyList())
    var filter by remember { mutableStateOf(PlanFilter.ALL) }
    val scope = rememberCoroutineScope()
    val zone = ZoneId.systemDefault()
    val now = System.currentTimeMillis()
    val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
    val startOfTomorrow = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    fun TaskEntity.matches(): Boolean {
        val due = dueAtEpochMillis
        return when (filter) {
            PlanFilter.ALL -> !completed
            PlanFilter.TODAY -> !completed && (due == null || due in startOfToday until startOfTomorrow || due < startOfToday)
            PlanFilter.UPCOMING -> !completed && due != null && due >= startOfTomorrow
            PlanFilter.OVERDUE -> !completed && due != null && due < now
            PlanFilter.COMPLETED -> completed
        }
    }

    fun ReminderEntity.matches(): Boolean {
        val at = triggerAtEpochMillis
        return when (filter) {
            PlanFilter.ALL -> !completed && !cancelled
            PlanFilter.TODAY -> !completed && !cancelled && at != null && (at in startOfToday until startOfTomorrow || at < startOfToday)
            PlanFilter.UPCOMING -> !completed && !cancelled && at != null && at >= startOfTomorrow
            PlanFilter.OVERDUE -> !completed && !cancelled && at != null && at < now
            PlanFilter.COMPLETED -> completed || cancelled
        }
    }

    val filteredTasks = tasks.filter { it.matches() }
    val filteredReminders = reminders.filter { it.matches() }

    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item { PageHeader(title = "Plans", subtitle = "Tasks and reminders") }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.padding(bottom = Spacing.md)
                ) {
                    PlanFilter.entries.forEach { f ->
                        AppFilterChip(
                            label = f.name.lowercase().replaceFirstChar { it.uppercase() },
                            selected = filter == f,
                            onClick = { filter = f }
                        )
                    }
                }
            }
            if (filteredTasks.isEmpty() && filteredReminders.isEmpty()) {
                item {
                    EmptyState(
                        title = "No plans here",
                        message = "Use Quick capture on Home to add something.",
                        icon = Icons.Outlined.TaskAlt
                    )
                }
            } else {
                if (filteredTasks.isNotEmpty()) {
                    item { SectionHeader(title = "Tasks") }
                    items(filteredTasks, key = { "t${it.id}" }) { task ->
                        PlanRow(
                            title = task.title,
                            supporting = buildString {
                                append(when (task.priority) { 1 -> "Low"; 2 -> "Medium"; 3 -> "High"; else -> "Task" })
                                formatPlanTime(task.dueAtEpochMillis)?.let { append(" · $it") }
                            },
                            completed = task.completed,
                            overdue = !task.completed && task.dueAtEpochMillis != null && task.dueAtEpochMillis < now,
                            onToggle = { scope.launch { viewModel.taskRepository.setCompleted(task.id, !task.completed) } },
                            onClick = { onOpenTask(task.id) }
                        )
                        AppDivider()
                    }
                }
                if (filteredReminders.isNotEmpty()) {
                    item { SectionHeader(title = "Reminders") }
                    items(filteredReminders, key = { "r${it.id}" }) { reminder ->
                        PlanRow(
                            title = reminder.title,
                            supporting = buildString {
                                append(if (reminder.cancelled) "Cancelled" else "Reminder")
                                formatPlanTime(reminder.triggerAtEpochMillis)?.let { append(" · $it") }
                                if (reminder.repeatType != ReminderEntity.REPEAT_NONE) append(" · ${reminder.repeatType}")
                            },
                            completed = reminder.completed,
                            overdue = !reminder.completed && !reminder.cancelled && reminder.triggerAtEpochMillis != null && reminder.triggerAtEpochMillis < now,
                            onToggle = { scope.launch { viewModel.reminderRepository.setCompleted(reminder.id, !reminder.completed) } },
                            onClick = { onOpenReminder(reminder.id) }
                        )
                        AppDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanRow(
    title: String,
    supporting: String,
    completed: Boolean,
    overdue: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconButton(
            icon = if (completed) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (completed) "Mark incomplete" else "Mark complete",
            onClick = onToggle
        )
        Column(
            modifier = Modifier.weight(1f).padding(vertical = Spacing.sm).then(if (completed) Modifier.alpha(0.55f) else Modifier)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (overdue && !completed) Warning else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NotesScreen(viewModel: HomeViewModel = viewModel()) {
    val notes by viewModel.noteRepository.observeNotes().collectAsStateWithLifecycle(emptyList())
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item { PageHeader(title = "Notes", subtitle = "Kept on this device") }
            if (notes.isEmpty()) {
                item { EmptyState(title = "No notes yet", message = "Capture a note from Quick capture.", icon = Icons.Outlined.Notes) }
            } else {
                items(notes, key = { it.id }) { note ->
                    AppListItem(title = note.title.ifBlank { "Untitled note" }, supportingText = note.body.take(80).ifBlank { null }, leadingIcon = Icons.Outlined.Notes)
                }
            }
        }
    }
}

@Composable
fun IdeasScreen(viewModel: HomeViewModel = viewModel()) {
    val ideas by viewModel.ideaRepository.observeIdeas().collectAsStateWithLifecycle(emptyList())
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item { PageHeader(title = "Ideas", subtitle = "Loose thoughts, saved") }
            if (ideas.isEmpty()) {
                item { EmptyState(title = "No ideas yet", message = "Park a thought from Quick capture.", icon = Icons.Outlined.Lightbulb) }
            } else {
                items(ideas, key = { it.id }) { idea ->
                    AppListItem(title = idea.title, supportingText = idea.body.take(80).ifBlank { null }, leadingIcon = Icons.Outlined.Lightbulb)
                }
            }
        }
    }
}
