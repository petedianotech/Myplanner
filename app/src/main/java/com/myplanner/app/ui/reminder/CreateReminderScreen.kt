package com.myplanner.app.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myplanner.app.data.local.ReminderEntity
import com.myplanner.app.ui.capture.CaptureFormScreen
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.DateTimePickerField
import com.myplanner.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateReminderScreen(
    onSave: suspend (title: String, notes: String, triggerAt: Long?, repeatType: String) -> Unit,
    onCancel: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    var triggerAt by remember {
        mutableStateOf<Long?>(ZonedDateTime.now(zone).plusHours(1).toInstant().toEpochMilli())
    }
    var repeatType by remember { mutableStateOf(ReminderEntity.REPEAT_NONE) }

    CaptureFormScreen(
        title = "New reminder",
        subtitle = "You'll get a local alert at the scheduled time.",
        primaryLabel = "Save reminder",
        fieldLabel = "Remind me to",
        fieldPlaceholder = "e.g. Pick up the kids",
        extraContent = {
            Spacer(Modifier.height(Spacing.lg))
            DateTimePickerField(
                label = "When",
                millis = triggerAt,
                onMillisChange = { triggerAt = it },
                allowClear = false,
                zone = zone
            )
            Spacer(Modifier.height(Spacing.md))
            Text("Quick picks", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppFilterChip(label = "In 1 hour", selected = false, onClick = {
                    triggerAt = ZonedDateTime.now(zone).plusHours(1).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Tomorrow 9:00", selected = false, onClick = {
                    triggerAt = LocalDate.now(zone).plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Tonight 20:00", selected = false, onClick = {
                    triggerAt = LocalDate.now(zone).atTime(20, 0).atZone(zone).toInstant().toEpochMilli()
                })
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
        },
        onSave = { title, notes -> onSave(title, notes, triggerAt, repeatType) },
        onCancel = onCancel
    )
}
