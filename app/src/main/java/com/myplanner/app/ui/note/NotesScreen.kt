package com.myplanner.app.ui.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.data.local.NoteEntity
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppFab
import com.myplanner.app.ui.components.AppFilterChip
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

private enum class NotesFilter { ACTIVE, ARCHIVED }

@Composable
fun NotesScreen(
    onOpenNote: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onOpenVoiceNotes: (() -> Unit)? = null,
    viewModel: HomeViewModel = viewModel()
) {
    val active by viewModel.noteRepository.observeActive()
        .collectAsStateWithLifecycle(emptyList())
    val archived by viewModel.noteRepository.observeArchived()
        .collectAsStateWithLifecycle(emptyList())
    var filter by remember { mutableStateOf(NotesFilter.ACTIVE) }
    val notes = if (filter == NotesFilter.ACTIVE) active else archived

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AppFab(icon = Icons.Outlined.Add, contentDescription = "New note", onClick = onCreateNote)
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal),
            contentPadding = PaddingValues(bottom = Spacing.huge)
        ) {
            item {
                PageHeader(title = "Notes", subtitle = "Kept on this device")
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    AppFilterChip(label = "Active", selected = filter == NotesFilter.ACTIVE, onClick = { filter = NotesFilter.ACTIVE })
                    AppFilterChip(label = "Archived", selected = filter == NotesFilter.ARCHIVED, onClick = { filter = NotesFilter.ARCHIVED })
                }
            }
            if (notes.isEmpty()) {
                item {
                    EmptyState(
                        title = if (filter == NotesFilter.ACTIVE) "No notes yet" else "No archived notes",
                        message = "Tap + to write a note.",
                        icon = Icons.Outlined.Notes
                    )
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    NoteRow(note = note, onClick = { onOpenNote(note.id) })
                    AppDivider()
                }
            }
        }
    }
}

@Composable
private fun NoteRow(note: NoteEntity, onClick: () -> Unit) {
    val preview = note.body.trim().replace('\n', ' ').take(100).ifBlank { null }
    val updated = formatRelativeUpdated(note.updatedAtEpochMillis)
    AppListItem(
        title = note.title.ifBlank { "Untitled note" },
        supportingText = listOfNotNull(preview, updated).joinToString(" · ").ifBlank { null },
        leadingIcon = Icons.Outlined.Notes,
        trailingContent = if (note.pinned) {
            {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        onClick = onClick
    )
}

private fun formatRelativeUpdated(millis: Long): String? {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
    }
}
