package com.myplanner.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppIconButton
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.home.PlanItem
import com.myplanner.app.ui.home.PlanKind
import com.myplanner.app.ui.organize.groupLabel
import com.myplanner.app.ui.organize.monthBounds
import com.myplanner.app.ui.organize.shortTime
import com.myplanner.app.ui.organize.toLocalDate
import com.myplanner.app.ui.theme.Spacing
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onOpenItem: (PlanItem) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val tasks by viewModel.taskRepository.observeTasks().collectAsStateWithLifecycle(emptyList())
    val reminders by viewModel.reminderRepository.observeReminders().collectAsStateWithLifecycle(emptyList())
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    var visibleMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selected by remember { mutableStateOf(today) }
    val (monthStart, monthEnd) = remember(visibleMonth) { monthBounds(visibleMonth, zone) }
    val itemsByDate by remember(tasks, reminders, visibleMonth) {
        derivedStateOf {
            val map = mutableMapOf<LocalDate, MutableList<PlanItem>>()
            tasks.forEach { task ->
                val due = task.dueAtEpochMillis ?: return@forEach
                if (due in monthStart until monthEnd) {
                    map.getOrPut(due.toLocalDate(zone)) { mutableListOf() }.add(
                        PlanItem(task.id, PlanKind.TASK, task.title, due, task.completed, task.priority)
                    )
                }
            }
            reminders.filter { !it.cancelled }.forEach { rem ->
                val at = rem.triggerAtEpochMillis ?: return@forEach
                if (at in monthStart until monthEnd) {
                    map.getOrPut(at.toLocalDate(zone)) { mutableListOf() }.add(
                        PlanItem(rem.id, PlanKind.REMINDER, rem.title, at, rem.completed)
                    )
                }
            }
            map.mapValues { (_, list) -> list.sortedWith(compareBy({ it.completed }, { it.atMillis ?: Long.MAX_VALUE })) }
        }
    }
    val dayItems = itemsByDate[selected].orEmpty()
    val now = System.currentTimeMillis()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                    PageHeader(title = "Calendar", subtitle = "Tasks and reminders by date", modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    AppIconButton(icon = Icons.Outlined.ChevronLeft, contentDescription = "Previous month", onClick = {
                        visibleMonth = visibleMonth.minusMonths(1)
                        selected = visibleMonth.atDay(1)
                    })
                    Text(
                        text = "${visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${visibleMonth.year}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    AppIconButton(icon = Icons.Outlined.ChevronRight, contentDescription = "Next month", onClick = {
                        visibleMonth = visibleMonth.plusMonths(1)
                        selected = visibleMonth.atDay(1)
                    })
                }
            }
            item {
                MonthGrid(month = visibleMonth, today = today, selected = selected, marked = itemsByDate.keys, onSelect = { selected = it })
            }
            item {
                Spacer(Modifier.height(Spacing.lg))
                Text(groupLabel(selected, today), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.sm))
            }
            if (dayItems.isEmpty()) {
                item {
                    EmptyState(
                        title = "Nothing on this day",
                        message = "Add a task or reminder to fill this date.",
                        icon = Icons.Outlined.Event,
                        action = { AppPrimaryButton(text = "Quick capture", onClick = onCreate, leadingIcon = Icons.Outlined.Add) }
                    )
                }
            } else {
                items(dayItems, key = { "${it.kind}-${it.id}" }) { item ->
                    AppListItem(
                        title = item.title,
                        supportingText = listOfNotNull(
                            if (item.kind == PlanKind.TASK) "Task" else "Reminder",
                            shortTime(item.atMillis),
                            if (item.completed) "Done" else if (item.isOverdue(now)) "Overdue" else null
                        ).joinToString(" · "),
                        leadingIcon = if (item.kind == PlanKind.TASK) Icons.Outlined.TaskAlt else Icons.Outlined.Alarm,
                        onClick = { onOpenItem(item) }
                    )
                    AppDivider()
                }
            }
            item { Spacer(Modifier.height(Spacing.xxxl)) }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    marked: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit
) {
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()
    val cells = lead + daysInMonth
    val rows = (cells + 6) / 7
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        var day = 1
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val index = row * 7 + col
                    if (index < lead || day > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = month.atDay(day)
                        val isSelected = date == selected
                        val isToday = date == today
                        val bg = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.background
                        }
                        val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                        Column(
                            modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(CircleShape).background(bg).clickable { onSelect(date) }
                                .semantics { contentDescription = "Date ${date.dayOfMonth}" },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal), color = fg)
                            Box(
                                Modifier.size(4.dp).clip(CircleShape).background(
                                    if (date in marked) {
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary
                                    } else MaterialTheme.colorScheme.background.copy(alpha = 0f)
                                )
                            )
                        }
                        day++
                    }
                }
            }
        }
    }
}
