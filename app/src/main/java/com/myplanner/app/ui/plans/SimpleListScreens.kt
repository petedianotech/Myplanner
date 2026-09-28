package com.myplanner.app.ui.plans

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.formatPlanTime
import com.myplanner.app.ui.theme.Spacing

@Composable
fun PlansScreen(viewModel: HomeViewModel = viewModel()) {
    val tasks by viewModel.taskRepository.observeTasks().collectAsStateWithLifecycle(emptyList())
    val reminders by viewModel.reminderRepository.observeReminders().collectAsStateWithLifecycle(emptyList())
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = MaterialTheme.colorScheme.background) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item { PageHeader(title = "Plans", subtitle = "Tasks and reminders in one place") }
            if (tasks.isEmpty() && reminders.isEmpty()) {
                item {
                    EmptyState(title = "No plans yet", message = "Use Quick capture on Home to add a task or reminder.", icon = Icons.Outlined.TaskAlt)
                }
            } else {
                items(tasks, key = { "t${it.id}" }) { task ->
                    AppListItem(
                        title = task.title,
                        supportingText = buildString {
                            append(if (task.completed) "Completed" else "Open")
                            formatPlanTime(task.dueAtEpochMillis)?.let { append(" · $it") }
                        },
                        leadingIcon = Icons.Outlined.TaskAlt
                    )
                }
                items(reminders, key = { "r${it.id}" }) { reminder ->
                    AppListItem(
                        title = reminder.title,
                        supportingText = formatPlanTime(reminder.triggerAtEpochMillis) ?: if (reminder.completed) "Done" else "Unscheduled",
                        leadingIcon = Icons.Outlined.TaskAlt
                    )
                }
            }
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
