package com.myplanner.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppFab
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppIconButton
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.theme.AppMotion
import com.myplanner.app.ui.theme.AppShapes
import com.myplanner.app.ui.theme.Spacing
import com.myplanner.app.ui.theme.Warning
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onQuickCapture: () -> Unit,
    onOpenItem: (PlanItem) -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenUpcoming: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()))
    val haptics = LocalHapticFeedback.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AppFab(icon = Icons.Outlined.Add, contentDescription = "Quick capture", onClick = onQuickCapture)
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
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item(key = "header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier = Modifier.weight(1f)) {
                            Text(
                                greetingForHour(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                dateLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AppIconButton(icon = Icons.Outlined.Search, contentDescription = "Search", onClick = onOpenSearch)
                        AppIconButton(icon = Icons.Outlined.CalendarMonth, contentDescription = "Calendar", onClick = onOpenCalendar)
                        AppIconButton(icon = Icons.Outlined.Settings, contentDescription = "Settings", onClick = onOpenSettings)
                    }
                }

                item(key = "hero") {
                    Spacer(Modifier.height(Spacing.sm))
                    TodayHeroCard(state)
                }

                stickyHeader(key = "filters") {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            contentPadding = PaddingValues(vertical = Spacing.md),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(HomeFilter.entries) { f ->
                                val label = when (f) {
                                    HomeFilter.ALL -> "All"
                                    HomeFilter.TODAY -> "Today"
                                    HomeFilter.OVERDUE -> if (state.overdueCount > 0) "Overdue · ${state.overdueCount}" else "Overdue"
                                    HomeFilter.UPCOMING -> "Upcoming"
                                }
                                AppFilterChip(
                                    label = label,
                                    selected = state.filter == f,
                                    onClick = { viewModel.setFilter(f) }
                                )
                            }
                        }
                    }
                }

                if (state.filteredItems.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = when (state.filter) {
                                HomeFilter.OVERDUE -> "Nothing overdue"
                                HomeFilter.UPCOMING -> "Nothing upcoming"
                                HomeFilter.TODAY -> "A clear day"
                                HomeFilter.ALL -> "Nothing planned yet"
                            },
                            message = when (state.filter) {
                                HomeFilter.OVERDUE -> "You're all caught up. Nice work."
                                HomeFilter.UPCOMING -> "Schedule something for later when you're ready."
                                else -> "Capture a task or reminder — it stays on this device only."
                            },
                            icon = Icons.Outlined.WbSunny,
                            action = {
                                AppPrimaryButton(
                                    text = "Quick capture",
                                    onClick = onQuickCapture,
                                    leadingIcon = Icons.Outlined.Add
                                )
                            }
                        )
                    }
                } else {
                    items(
                        items = state.filteredItems,
                        key = { "${it.kind}-${it.id}" }
                    ) { item ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(AppMotion.mediumTween()) + expandVertically(AppMotion.mediumTween()),
                            exit = fadeOut(AppMotion.shortTween()) + shrinkVertically(AppMotion.shortTween())
                        ) {
                            PlanItemCard(
                                item = item,
                                onToggle = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleItem(item)
                                },
                                onOpen = { onOpenItem(item) },
                                onSnooze = { minutes ->
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.snoozeReminder(item, minutes)
                                }
                            )
                        }
                    }

                    if (state.filter == HomeFilter.ALL && state.upcomingItems.isNotEmpty()) {
                        item(key = "see_all") {
                            TextButton(
                                onClick = onOpenUpcoming,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("See all upcoming")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayHeroCard(state: HomeUiState) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.cardLarge)
            .background(
                Brush.linearGradient(
                    listOf(
                        primary.copy(alpha = 0.92f),
                        secondary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(Spacing.xl)
    ) {
        Column {
            val open = state.todayTasksRemaining + state.todayRemindersScheduled
            Text(
                text = when {
                    state.overdueCount > 0 -> "${state.overdueCount} overdue · $open open today"
                    open == 0 -> "All clear for today"
                    open == 1 -> "1 item still open"
                    else -> "$open items still open"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = when {
                    state.overdueCount > 0 -> "Start with what's overdue — the rest can wait."
                    open == 0 && state.completedTodayCount > 0 ->
                        "You finished ${state.completedTodayCount} today. Rest or capture something new."
                    open == 0 -> "A quiet day. Capture when something comes to mind."
                    else -> {
                        val tasks = when (state.todayTasksRemaining) {
                            0 -> "No tasks"
                            1 -> "1 task"
                            else -> "${state.todayTasksRemaining} tasks"
                        }
                        val reminders = when (state.todayRemindersScheduled) {
                            0 -> "no reminders"
                            1 -> "1 reminder"
                            else -> "${state.todayRemindersScheduled} reminders"
                        }
                        "$tasks · $reminders"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            if (state.completedTodayCount > 0 || state.overdueCount > 0) {
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (state.completedTodayCount > 0) {
                        StatPill("${state.completedTodayCount} done")
                    }
                    if (state.overdueCount > 0) {
                        StatPill("${state.overdueCount} overdue")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PlanItemCard(
    item: PlanItem,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    onSnooze: (Long) -> Unit = {}
) {
    val overdue = item.isOverdue(System.currentTimeMillis())
    val timeLabel = formatPlanTime(item.atMillis)
    var snoozeMenu by remember { mutableStateOf(false) }
    val checkScale by animateFloatAsState(
        targetValue = if (item.completed) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "checkScale"
    )
    val accent = when {
        item.completed -> MaterialTheme.colorScheme.outlineVariant
        overdue -> Warning
        item.priority >= 3 -> MaterialTheme.colorScheme.error
        item.priority == 2 -> MaterialTheme.colorScheme.tertiary
        item.kind == PlanKind.REMINDER -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.card)
            .clickable(onClick = onOpen),
        color = MaterialTheme.colorScheme.surface,
        shape = AppShapes.card,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .background(accent)
            )
            AppIconButton(
                icon = if (item.completed) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = if (item.completed) "Mark incomplete" else "Mark complete",
                onClick = onToggle,
                modifier = Modifier.scale(checkScale)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.md)
                    .then(if (item.completed) Modifier.alpha(0.5f) else Modifier)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (overdue && !item.completed) Warning else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.kind == PlanKind.TASK) Icons.Outlined.TaskAlt else Icons.Outlined.Alarm,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = buildString {
                            append(if (item.kind == PlanKind.TASK) "Task" else "Reminder")
                            if (timeLabel != null) append(" · $timeLabel")
                            if (overdue) append(" · Overdue")
                            if (item.priority >= 2 && item.kind == PlanKind.TASK) {
                                append(" · ")
                                append(when (item.priority) { 2 -> "Medium"; 3 -> "High"; else -> "" })
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (item.kind == PlanKind.REMINDER && !item.completed) {
                Box {
                    AppIconButton(
                        icon = Icons.Outlined.Snooze,
                        contentDescription = "Snooze",
                        onClick = { snoozeMenu = true }
                    )
                    DropdownMenu(
                        expanded = snoozeMenu,
                        onDismissRequest = { snoozeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("10 minutes") },
                            onClick = { snoozeMenu = false; onSnooze(10) }
                        )
                        DropdownMenuItem(
                            text = { Text("30 minutes") },
                            onClick = { snoozeMenu = false; onSnooze(30) }
                        )
                        DropdownMenuItem(
                            text = { Text("1 hour") },
                            onClick = { snoozeMenu = false; onSnooze(60) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

fun greetingForHour(hour: Int = LocalTime.now().hour): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Hello"
}

fun formatPlanTime(millis: Long?, zone: ZoneId = ZoneId.systemDefault()): String? {
    if (millis == null) return null
    val zdt = Instant.ofEpochMilli(millis).atZone(zone)
    val today = LocalDate.now(zone)
    val date = zdt.toLocalDate()
    val time = "%02d:%02d".format(zdt.hour, zdt.minute)
    return when (date) {
        today -> time
        today.plusDays(1) -> "Tomorrow · $time"
        today.minusDays(1) -> "Yesterday · $time"
        else -> {
            val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            "${date.dayOfMonth} $month · $time"
        }
    }
}
