package com.myplanner.app.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.TaskEntity
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditorScreen(
    taskId: Long,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val existing by viewModel.taskRepository.observeTask(taskId).collectAsStateWithLifecycle(null)
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueAt by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val zone = ZoneId.systemDefault()

    LaunchedEffect(existing) {
        existing?.let { t ->
            if (!loaded) {
                title = t.title
                notes = t.notes
                dueAt = t.dueAtEpochMillis
                priority = t.priority
                completed = t.completed
                loaded = true
            }
        }
    }

    if (existing == null && !loaded) {
        LoadingState(message = "Loading task…")
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxxl)
        ) {
            PageHeader(title = "Edit task", subtitle = if (completed) "Completed" else "Open")
            Spacer(Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = { title = it; error = false },
                label = "Task",
                isError = error,
                supportingText = if (error) "Title is required" else null,
                singleLine = true
            )
            Spacer(Modifier.height(Spacing.md))
            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes (optional)",
                singleLine = false,
                maxLines = 5
            )
            Spacer(Modifier.height(Spacing.lg))
            Text("Priority", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(0 to "None", 1 to "Low", 2 to "Medium", 3 to "High").forEach { (v, label) ->
                    AppFilterChip(label = label, selected = priority == v, onClick = { priority = v })
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text("Due", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppFilterChip(label = "Today", selected = false, onClick = {
                    dueAt = LocalDate.now(zone).atTime(18, 0).atZone(zone).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Tomorrow", selected = false, onClick = {
                    dueAt = LocalDate.now(zone).plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Clear", selected = false, onClick = { dueAt = null })
            }
            Spacer(Modifier.height(Spacing.section))
            AppPrimaryButton(
                text = if (saving) "Saving…" else "Save changes",
                onClick = {
                    if (title.isBlank()) { error = true; return@AppPrimaryButton }
                    scope.launch {
                        saving = true
                        try {
                            val base = existing ?: TaskEntity(id = taskId, title = title)
                            viewModel.taskRepository.updateTask(
                                base.copy(
                                    title = title.trim(),
                                    notes = notes.trim(),
                                    dueAtEpochMillis = dueAt,
                                    priority = priority,
                                    completed = completed
                                )
                            )
                            onDone()
                        } finally { saving = false }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))
            AppTextButton(
                text = if (completed) "Mark incomplete" else "Mark complete",
                onClick = {
                    scope.launch {
                        viewModel.taskRepository.setCompleted(taskId, !completed)
                        onDone()
                    }
                }
            )
            AppTextButton(text = "Delete task", onClick = { showDelete = true })
            AppTextButton(text = "Cancel", onClick = onCancel, enabled = !saving)
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this task?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.taskRepository.deleteTask(taskId)
                        showDelete = false
                        onDone()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}
