package com.myplanner.app.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myplanner.app.ui.capture.CaptureFormScreen
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTaskScreen(
    onSave: suspend (title: String, notes: String, dueAt: Long?, priority: Int) -> Unit,
    onCancel: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    var dueAt by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableIntStateOf(0) }

    CaptureFormScreen(
        title = "New task",
        subtitle = "Capture what needs doing.",
        primaryLabel = "Save task",
        fieldLabel = "Task",
        fieldPlaceholder = "e.g. Draft the weekly plan",
        extraContent = {
            Spacer(Modifier.height(Spacing.lg))
            Text("Priority", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(0 to "None", 1 to "Low", 2 to "Medium", 3 to "High").forEach { (v, label) ->
                    AppFilterChip(label = label, selected = priority == v, onClick = { priority = v })
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text("Due (optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppFilterChip(label = "Today", selected = false, onClick = {
                    dueAt = LocalDate.now(zone).atTime(18, 0).atZone(zone).toInstant().toEpochMilli()
                })
                AppFilterChip(label = "Tomorrow", selected = false, onClick = {
                    dueAt = LocalDate.now(zone).plusDays(1).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
                })
                if (dueAt != null) {
                    AppFilterChip(label = "Clear due", selected = false, onClick = { dueAt = null })
                }
            }
        },
        onSave = { title, notes -> onSave(title, notes, dueAt, priority) },
        onCancel = onCancel
    )
}
