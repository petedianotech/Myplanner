package com.myplanner.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppCard
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppFab
import com.myplanner.app.ui.components.AppIconButton
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.theme.Spacing
import com.myplanner.app.ui.theme.Warning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onQuickCapture: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateLabel = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AppFab(
                icon = Icons.Outlined.Add,
                contentDescription = "Quick capture",
                onClick = onQuickCapture
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(innerPadding), message = "Loading your day…")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    top = Spacing.lg,
                    bottom = Spacing.huge
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    Text(greetingForHour(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text(dateLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item { TodaySummaryCard(state) }
                if (state.todayItems.isEmpty() && state.upcomingItems.isEmpty()) {
                    item {
                        AppCard {
                            EmptyState(
                                title = "Nothing planned today",
                                message = "Capture a task or reminder to give this day a shape.",
                                action = {
                                    AppPrimaryButton(text = "Quick capture", onClick = onQuickCapture, leadingIcon = Icons.Outlined.Add)
                                }
                            )
                        }
                    }
                } else {
                    item {
                        SectionHeader(
                            title = "Today",
                            subtitle = if (state.overdueCount > 0) "${state.overdueCount} overdue" else null
                        )
                    }
                    items(state.todayItems, key = { "${it.kind}-${it.id}" }) { item ->
                        PlanRow(item) { viewModel.toggleItem(item) }
                        AppDivider()
                    }
                    if (state.upcomingItems.isNotEmpty()) {
                        item { SectionHeader(title = "Upcoming") }
                        items(state.upcomingItems, key = { "up-${it.kind}-${it.id}" }) { item ->
                            PlanRow(item) { viewModel.toggleItem(item) }
                            AppDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(state: HomeUiState) {
    AppCard {
        val tasks = when (state.todayTasksRemaining) {
            0 -> "No tasks left"
            1 -> "1 task remaining"
            else -> "${state.todayTasksRemaining} tasks remaining"
        }
        val reminders = when (state.todayRemindersScheduled) {
            0 -> "no reminders"
            1 -> "1 reminder"
            else -> "${state.todayRemindersScheduled} reminders"
        }
        Text("$tasks · $reminders", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = when {
                state.overdueCount > 0 -> "A few things are overdue — tackle those first if you can."
                state.todayTasksRemaining == 0 && state.todayRemindersScheduled == 0 -> "A quiet day. Capture something when it comes to mind."
                else -> "Stay with the next item. The rest can wait."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanRow(item: PlanItem, onToggle: () -> Unit) {
    val overdue = item.isOverdue(System.currentTimeMillis())
    val timeLabel = formatPlanTime(item.atMillis)
    val supporting = buildString {
        append(if (item.kind == PlanKind.TASK) "Task" else "Reminder")
        if (timeLabel != null) append(" · $timeLabel")
        if (overdue) append(" · Overdue")
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AppIconButton(
            icon = if (item.completed) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (item.completed) "Mark incomplete" else "Mark complete",
            onClick = onToggle
        )
        Column(
            modifier = Modifier.weight(1f).padding(vertical = Spacing.sm).then(if (item.completed) Modifier.alpha(0.55f) else Modifier)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (overdue && !item.completed) Warning else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        androidx.compose.material3.Icon(
            imageVector = if (item.kind == PlanKind.TASK) Icons.Outlined.TaskAlt else Icons.Outlined.Alarm,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
