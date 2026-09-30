package com.myplanner.app.ui.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun NoteEditorScreen(
    noteId: Long?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val existing by if (noteId != null) {
        viewModel.noteRepository.observeNote(noteId).collectAsStateWithLifecycle(null)
    } else {
        remember { mutableStateOf(null) }
    }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var pinned by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(noteId == null) }
    var saving by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(existing) {
        existing?.let { n ->
            if (!loaded) {
                title = n.title
                body = n.body
                pinned = n.pinned
                loaded = true
            }
        }
    }

    if (noteId != null && existing == null && !loaded) {
        LoadingState(message = "Loading note…")
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxxl)
        ) {
            PageHeader(
                title = if (noteId == null) "New note" else "Edit note",
                subtitle = "Saved on this device"
            )
            Spacer(Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = { title = it; error = false },
                label = "Title",
                isError = error,
                supportingText = if (error) "Title is required" else null,
                singleLine = true
            )
            Spacer(Modifier.height(Spacing.md))
            AppTextField(
                value = body,
                onValueChange = { body = it },
                label = "Note",
                singleLine = false,
                maxLines = 12
            )
            Spacer(Modifier.height(Spacing.xxl))
            AppPrimaryButton(
                text = if (saving) "Saving…" else "Save note",
                onClick = {
                    if (title.isBlank()) { error = true; return@AppPrimaryButton }
                    saving = true
                    scope.launch {
                        if (noteId == null) {
                            viewModel.noteRepository.createNote(title.trim(), body.trim())
                        } else {
                            viewModel.noteRepository.updateNote(existing!!.copy(title = title.trim(), body = body.trim(), pinned = pinned))
                        }
                        onDone()
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))
            AppTextButton(text = "Cancel", onClick = onCancel, modifier = Modifier.fillMaxWidth())
            if (noteId != null) {
                Spacer(Modifier.height(Spacing.lg))
                AppTextButton(
                    text = "Delete note",
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDelete && noteId != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete note?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.noteRepository.deleteNote(noteId)
                        onDone()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}
