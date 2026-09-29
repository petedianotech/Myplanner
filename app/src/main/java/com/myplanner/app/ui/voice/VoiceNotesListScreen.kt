package com.myplanner.app.ui.voice

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.VoiceNoteEntity
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppFab
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun VoiceNotesListScreen(
    onOpen: (Long) -> Unit,
    onRecord: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val notes by viewModel.voiceNoteRepository.observeVoiceNotes()
        .collectAsStateWithLifecycle(emptyList())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AppFab(
                icon = Icons.Outlined.Add,
                contentDescription = "Record voice note",
                onClick = onRecord
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item {
                PageHeader(
                    title = "Voice notes",
                    subtitle = "Private recordings on this device"
                )
            }
            if (notes.isEmpty()) {
                item {
                    EmptyState(
                        title = "No voice notes yet",
                        message = "Capture a quick memo from Quick capture or the + button.",
                        icon = Icons.Outlined.Mic
                    )
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    VoiceNoteRow(note = note, onClick = { onOpen(note.id) })
                    AppDivider()
                }
            }
        }
    }
}

@Composable
private fun VoiceNoteRow(
    note: VoiceNoteEntity,
    onClick: () -> Unit
) {
    AppListItem(
        title = note.title,
        supportingText = listOf(
            formatVoiceDate(note.createdAtEpochMillis),
            formatDuration(note.durationMillis)
        ).joinToString(" · "),
        leadingIcon = Icons.Outlined.Mic,
        trailingContent = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        onClick = onClick
    )
}

internal fun formatVoiceDate(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    val time = Instant.ofEpochMilli(millis).atZone(zone)
    val timeStr = "%02d:%02d".format(time.hour, time.minute)
    return when (date) {
        today -> "Today · $timeStr"
        today.minusDays(1) -> "Yesterday · $timeStr"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())) + " · $timeStr"
    }
}
