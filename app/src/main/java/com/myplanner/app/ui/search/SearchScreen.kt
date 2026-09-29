package com.myplanner.app.ui.search

import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppDivider
import com.myplanner.app.ui.components.AppListItem
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.EmptyState
import com.myplanner.app.ui.components.SectionHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf

enum class SearchKind { TASK, REMINDER, NOTE, IDEA, VOICE }

data class SearchHit(
    val id: Long,
    val kind: SearchKind,
    val title: String,
    val snippet: String?
)

@Composable
fun SearchScreen(
    onOpenTask: (Long) -> Unit,
    onOpenReminder: (Long) -> Unit,
    onOpenNote: (Long) -> Unit,
    onOpenIdea: (Long) -> Unit,
    onOpenVoice: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var query by rememberSaveable { mutableStateOf("") }
    var debounced by remember { mutableStateOf("") }
    LaunchedEffect(query) {
        delay(280)
        debounced = query.trim()
    }
    val trimmed = query.trim()
    val active = debounced.length >= 2

    val tasksFlow = if (active) viewModel.taskRepository.search(debounced) else flowOf(emptyList())
    val remindersFlow = if (active) viewModel.reminderRepository.search(debounced) else flowOf(emptyList())
    val notesFlow = if (active) viewModel.noteRepository.search(debounced) else flowOf(emptyList())
    val ideasFlow = if (active) viewModel.ideaRepository.search(debounced) else flowOf(emptyList())
    val voicesFlow = if (active) viewModel.voiceNoteRepository.search(debounced) else flowOf(emptyList())
    val tasks by tasksFlow.collectAsStateWithLifecycle(emptyList())
    val reminders by remindersFlow.collectAsStateWithLifecycle(emptyList())
    val notes by notesFlow.collectAsStateWithLifecycle(emptyList())
    val ideas by ideasFlow.collectAsStateWithLifecycle(emptyList())
    val voices by voicesFlow.collectAsStateWithLifecycle(emptyList())

    val groups = remember(tasks, reminders, notes, ideas, voices) {
        buildList {
            if (tasks.isNotEmpty()) add(
                "Tasks" to tasks.map {
                    SearchHit(it.id, SearchKind.TASK, it.title.ifBlank { "Untitled task" }, it.notes.takeIf { n -> n.isNotBlank() }?.take(80))
                }
            )
            if (reminders.isNotEmpty()) add(
                "Reminders" to reminders.map {
                    SearchHit(it.id, SearchKind.REMINDER, it.title, it.notes.takeIf { n -> n.isNotBlank() }?.take(80))
                }
            )
            if (notes.isNotEmpty()) add(
                "Notes" to notes.map {
                    SearchHit(it.id, SearchKind.NOTE, it.title.ifBlank { "Untitled note" }, it.body.takeIf { b -> b.isNotBlank() }?.take(80))
                }
            )
            if (ideas.isNotEmpty()) add(
                "Ideas" to ideas.map {
                    SearchHit(it.id, SearchKind.IDEA, it.title, listOfNotNull(it.category.takeIf { c -> c.isNotBlank() }, it.status).joinToString(" · "))
                }
            )
            if (voices.isNotEmpty()) add(
                "Voice notes" to voices.map {
                    SearchHit(it.id, SearchKind.VOICE, it.title, it.description.takeIf { d -> d.isNotBlank() })
                }
            )
        }
    }

    val focus = FocusRequester()
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focus.requestFocus() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(horizontal = Spacing.screenHorizontal)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search plans, notes and ideas",
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                        }
                    }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier.focusRequester(focus)
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Spacing.huge)
            ) {
                when {
                    trimmed.isEmpty() -> item {
                        EmptyState(
                            title = "Search your planner",
                            message = "Find tasks, reminders, notes, ideas and voice notes stored on this device.",
                            icon = Icons.Outlined.Search
                        )
                    }
                    trimmed.length < 2 -> item {
                        Text(
                            text = "Type at least two characters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    groups.isEmpty() && active -> item {
                        EmptyState(
                            title = "No matches",
                            message = "Nothing locally matches this search. Try a different word.",
                            icon = Icons.Outlined.Search
                        )
                    }
                    !active -> item {
                        Text(
                            text = "Type at least two characters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        groups.forEach { (label, hits) ->
                            item { SectionHeader(title = label) }
                            items(hits, key = { "${it.kind}-${it.id}" }) { hit ->
                                AppListItem(
                                    title = hit.title,
                                    supportingText = hit.snippet,
                                    leadingIcon = iconFor(hit.kind),
                                    onClick = {
                                        when (hit.kind) {
                                            SearchKind.TASK -> onOpenTask(hit.id)
                                            SearchKind.REMINDER -> onOpenReminder(hit.id)
                                            SearchKind.NOTE -> onOpenNote(hit.id)
                                            SearchKind.IDEA -> onOpenIdea(hit.id)
                                            SearchKind.VOICE -> onOpenVoice(hit.id)
                                        }
                                    }
                                )
                                AppDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun iconFor(kind: SearchKind): ImageVector = when (kind) {
    SearchKind.TASK -> Icons.Outlined.TaskAlt
    SearchKind.REMINDER -> Icons.Outlined.Alarm
    SearchKind.NOTE -> Icons.Outlined.Notes
    SearchKind.IDEA -> Icons.Outlined.Lightbulb
    SearchKind.VOICE -> Icons.Outlined.Mic
}
