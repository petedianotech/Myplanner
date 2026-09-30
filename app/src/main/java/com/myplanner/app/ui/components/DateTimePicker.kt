package com.myplanner.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val WheelItemHeight = 44.dp
private const val VisibleItems = 5

/**
 * Engaging date + sliding hour/minute picker.
 * Users scroll wheels to pick exact time; date chips for quick day selection.
 */
@Composable
fun DateTimePickerDialog(
    initialMillis: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Pick date & time",
    zone: ZoneId = ZoneId.systemDefault()
) {
    val initial = remember(initialMillis) {
        initialMillis?.let { Instant.ofEpochMilli(it).atZone(zone) }
            ?: java.time.ZonedDateTime.now(zone).plusHours(1)
    }
    var selectedDate by remember { mutableStateOf(initial.toLocalDate()) }
    var hour by remember { mutableIntStateOf(initial.hour) }
    var minute by remember { mutableIntStateOf((initial.minute / 5) * 5) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(Spacing.lg)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.md))

            // Date chips: Today, Tomorrow, + next 5 days
            val today = LocalDate.now(zone)
            val dates = remember(today) {
                (0..6).map { today.plusDays(it.toLong()) }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(vertical = Spacing.xs)
            ) {
                items(dates, key = { it.toEpochDay() }) { date ->
                    val selected = date == selectedDate
                    val label = when (date) {
                        today -> "Today"
                        today.plusDays(1) -> "Tomorrow"
                        else -> date.format(DateTimeFormatter.ofPattern("EEE d", Locale.getDefault()))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedDate = date }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            // Live preview
            val preview = selectedDate.atTime(hour, minute)
            Text(
                text = preview.format(
                    DateTimeFormatter.ofPattern("EEE, d MMM · HH:mm", Locale.getDefault())
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.md))

            // Sliding wheels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight * VisibleItems),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelColumn(
                    values = (0..23).toList(),
                    selected = hour,
                    onSelected = { hour = it },
                    label = { "%02d".format(it) },
                    modifier = Modifier.width(88.dp)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                WheelColumn(
                    values = (0..55 step 5).toList(),
                    selected = minute,
                    onSelected = { minute = it },
                    label = { "%02d".format(it) },
                    modifier = Modifier.width(88.dp)
                )
            }

            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "Slide to set the exact hour and minute",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(
                    onClick = {
                        val millis = selectedDate.atTime(hour, minute)
                            .atZone(zone).toInstant().toEpochMilli()
                        onConfirm(millis)
                    }
                ) { Text("Set time") }
            }
        }
    }
}

@Composable
private fun WheelColumn(
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit,
    label: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val initialIndex = values.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val scope = rememberCoroutineScope()
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    // Center padding so first/last items can sit in the middle
    val verticalPad = WheelItemHeight * ((VisibleItems - 1) / 2)

    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val center = layout.viewportStartOffset + layout.viewportSize.height / 2
            layout.visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - center) }?.index
        }
            .distinctUntilChanged()
            .collect { index ->
                if (index != null && index in values.indices) {
                    onSelected(values[index])
                }
            }
    }

    // Snap to initial
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Selection highlight band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WheelItemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    RoundedCornerShape(12.dp)
                )
        )
        LazyColumn(
            state = listState,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = verticalPad),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(values.size) { index ->
                val value = values[index]
                val isSelected = value == selected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight)
                        .clickable {
                            scope.launch { listState.animateScrollToItem(index) }
                            onSelected(value)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(value),
                        fontSize = if (isSelected) 28.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.45f)
                    )
                }
            }
        }
    }
}

/** Compact row that shows current time and opens the picker. */
@Composable
fun DateTimePickerField(
    label: String,
    millis: Long?,
    onMillisChange: (Long?) -> Unit,
    allowClear: Boolean = true,
    zone: ZoneId = ZoneId.systemDefault()
) {
    var showPicker by remember { mutableStateOf(false) }
    val display = remember(millis) {
        millis?.let {
            Instant.ofEpochMilli(it).atZone(zone)
                .format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", Locale.getDefault()))
        } ?: "Not set"
    }

    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showPicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = display,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (millis != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (allowClear && millis != null) {
                TextButton(onClick = { onMillisChange(null) }) { Text("Clear") }
            }
        }
    }

    if (showPicker) {
        DateTimePickerDialog(
            initialMillis = millis,
            onConfirm = {
                onMillisChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}
