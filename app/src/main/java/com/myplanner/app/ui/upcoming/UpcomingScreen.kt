package com.myplanner.app.ui.upcoming

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.PlanItem
import com.myplanner.app.ui.home.PlanKind
import com.myplanner.app.ui.organize.groupLabel
import com.myplanner.app.ui.organize.shortTime
import com.myplanner.app.ui.organize.toLocalDate
import com.myplanner.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun UpcomingScreen(
    onOpenItem: (PlanItem) -> Unit,
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val tasks by viewModel.taskRepository.observeTasks().collectAsStateWithLifecycle(emptyList())
    val reminders by viewModel.reminderRepository.observeReminders()
        .collectAsStateWithLifecycle(emptyList())

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val now = System.currentTimeMillis()

    val sections = remember(tasks, reminders) {
        val items = buildList {
            tasks.filter { !it.completed && it.dueAtEpochMillis != null }.forEach { t ->
                add(PlanItem(t.id, PlanKind.TASK, t.title, t.dueAtEpochMillis, false, t.priority))
            }
            reminders.filter { !it.completed && !it.cancelled && it.triggerAtEpochMillis != null }.forEach { r ->
                add(PlanItem(r.id, PlanKind.REMINDER, r.title, r.triggerAtEpochMillis, false))
            }
        }.sortedBy { it.atMillis ?: Long.MAX_VALUE }

        items.groupBy { item ->
            val at = item.atMillis ?: return@groupBy today
            at.toLocalDate(zone)
        }.toSortedMap()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    PageHeader(
                        title = "Upcoming",
                        subtitle = "What is next, in order",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (sections.isEmpty()) {
                item {
                    EmptyState(
                        title = "No upcoming plans",
                        message = "When you add a dated task or reminder, it will appear here.",
                        icon = Icons.Outlined.Event
                    )
                }
            } else {
                sections.forEach { (date, list) ->
                    item(key = "h-$date") {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        SectionHeader(title = groupLabel(date, today))
                    }
                    items(list, key = { "${it.kind}-${it.id}" }) { item ->
                        val overdue = item.isOverdue(now)
                        AppListItem(
                            title = item.title,
                            supportingText = listOfNotNull(
                                if (item.kind == PlanKind.TASK) "Task" else "Reminder",
                                shortTime(item.atMillis),
                                if (overdue && date.isBefore(today)) "Overdue" else null
                            ).joinToString(" · "),
                            leadingIcon = if (item.kind == PlanKind.TASK) Icons.Outlined.TaskAlt else Icons.Outlined.Alarm,
                            onClick = { onOpenItem(item) }
                        )
                        AppDivider()
                    }
                }
            }
        }
    }
}
