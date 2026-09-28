package com.myplanner.app.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myplanner.app.ui.capture.CaptureFormScreen
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.home.formatPlanTime
import com.myplanner.app.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateReminderScreen(
    onSave: suspend (title: String, notes: String, triggerAt: Long?) -> Unit,
    onCancel: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    var triggerAt by remember { mutableStateOf<Long?>(null) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(is24Hour = true)

    CaptureFormScreen(
        title = "New reminder",
        subtitle = "You'll get a local alert when the time arrives.",
        primaryLabel = "Save reminder",
        fieldLabel = "Remind me to",
        fieldPlaceholder = "e.g. Pick up the kids",
        extraContent = {
            Spacer(modifier = Modifier.height(Spacing.lg))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                AppFilterChip(label = "Later today", selected = false, onClick = {
                    triggerAt = ZonedDateTime.now(zone).plusHours(3).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Tomorrow 9:00", selected = false, onClick = {
                    triggerAt = LocalDate.now(zone).plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Pick date", selected = triggerAt != null, onClick = { showDate = true })
            }
            if (triggerAt != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Scheduled · ${formatPlanTime(triggerAt)}",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        },
        onSave = { title, notes -> onSave(title, notes, triggerAt) },
        onCancel = onCancel
    )

    if (showDate) {
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = dateState.selectedDateMillis
                    showDate = false
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                        val current = triggerAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() } ?: LocalTime.of(9, 0)
                        triggerAt = date.atTime(current).atZone(zone).toInstant().toEpochMilli()
                        showTime = true
                    }
                }) { Text("Next") }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
    if (showTime) {
        DatePickerDialog(
            onDismissRequest = { showTime = false },
            confirmButton = {
                TextButton(onClick = {
                    val base = triggerAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: LocalDate.now(zone)
                    triggerAt = base.atTime(timeState.hour, timeState.minute).atZone(zone).toInstant().toEpochMilli()
                    showTime = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Skip") } }
        ) { TimePicker(state = timeState) }
    }
}
