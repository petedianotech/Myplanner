package com.myplanner.app.ui.reminder

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.ReminderEntity
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.formatPlanTime
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderEditorScreen(
    reminderId: Long,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val existing by viewModel.reminderRepository.observeReminder(reminderId)
        .collectAsStateWithLifecycle(null)
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var triggerAt by remember { mutableStateOf<Long?>(null) }
    var repeatType by remember { mutableStateOf(ReminderEntity.REPEAT_NONE) }
    var completed by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val zone = ZoneId.systemDefault()

    LaunchedEffect(existing) {
        existing?.let { r ->
            if (!loaded) {
                title = r.title
                notes = r.notes
                triggerAt = r.triggerAtEpochMillis
                repeatType = r.repeatType
                completed = r.completed
                loaded = true
            }
        }
    }

    if (existing == null && !loaded) {
        LoadingState(message = "Loading reminder…")
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
            PageHeader(
                title = "Edit reminder",
                subtitle = formatPlanTime(triggerAt) ?: "Unscheduled"
            )
            Spacer(Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = { title = it; error = false },
                label = "Remind me to",
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
                maxLines = 4
            )
            Spacer(Modifier.height(Spacing.lg))
            Text("When", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppFilterChip(label = "In 1 hour", selected = false, onClick = {
                    triggerAt = ZonedDateTime.now(zone).plusHours(1).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Tomorrow 9:00", selected = false, onClick = {
                    triggerAt = LocalDate.now(zone).plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
                })
            }
            if (triggerAt != null) {
                Spacer(Modifier.height(Spacing.sm))
                Text(
                    text = "Scheduled · ${formatPlanTime(triggerAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text("Repeat", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(
                    ReminderEntity.REPEAT_NONE to "Once",
                    ReminderEntity.REPEAT_DAILY to "Daily",
                    ReminderEntity.REPEAT_WEEKLY to "Weekly",
                    ReminderEntity.REPEAT_MONTHLY to "Monthly"
                ).forEach { (value, label) ->
                    AppFilterChip(label = label, selected = repeatType == value, onClick = { repeatType = value })
                }
            }
            Spacer(Modifier.height(Spacing.section))
            AppPrimaryButton(
                text = if (saving) "Saving…" else "Save changes",
                onClick = {
                    if (title.isBlank()) { error = true; return@AppPrimaryButton }
                    scope.launch {
                        saving = true
                        try {
                            val base = existing ?: ReminderEntity(id = reminderId, title = title)
                            viewModel.reminderRepository.updateReminder(
                                base.copy(
                                    title = title.trim(),
                                    notes = notes.trim(),
                                    triggerAtEpochMillis = triggerAt,
                                    repeatType = repeatType,
                                    completed = completed,
                                    cancelled = false
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
                        viewModel.reminderRepository.setCompleted(reminderId, !completed)
                        onDone()
                    }
                }
            )
            AppTextButton(
                text = "Cancel reminder",
                onClick = {
                    scope.launch {
                        viewModel.reminderRepository.cancelReminder(reminderId)
                        onDone()
                    }
                }
            )
            AppTextButton(text = "Delete", onClick = { showDelete = true })
            AppTextButton(text = "Back", onClick = onCancel, enabled = !saving)
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this reminder?") },
            text = { Text("The scheduled alarm will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.reminderRepository.deleteReminder(reminderId)
                        showDelete = false
                        onDone()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Keep") }
            }
        )
    }
}
