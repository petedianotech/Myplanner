package com.myplanner.app.ui.idea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.myplanner.app.data.local.IdeaEntity
import com.myplanner.app.ui.components.AppFilterChip
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.LoadingState
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdeaEditorScreen(
    ideaId: Long?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val existing by if (ideaId != null) {
        viewModel.ideaRepository.observeIdea(ideaId).collectAsStateWithLifecycle(null)
    } else {
        remember { mutableStateOf(null) }
    }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(IdeaEntity.CATEGORY_OTHER) }
    var status by remember { mutableStateOf(IdeaEntity.STATUS_NEW) }
    var pinned by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(ideaId == null) }
    var saving by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(existing) {
        existing?.let { i ->
            if (!loaded) {
                title = i.title
                body = i.body
                category = i.category
                status = i.status
                pinned = i.pinned
                loaded = true
            }
        }
    }

    if (ideaId != null && existing == null && !loaded) {
        LoadingState(message = "Loading idea…")
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
                title = if (ideaId == null) "New idea" else "Edit idea",
                subtitle = "Catch it before it fades"
            )
            Spacer(Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = { title = it; error = false },
                label = "Idea",
                isError = error,
                supportingText = if (error) "Title is required" else null,
                singleLine = true
            )
            Spacer(Modifier.height(Spacing.md))
            AppTextField(
                value = body,
                onValueChange = { body = it },
                label = "Details (optional)",
                singleLine = false,
                maxLines = 8
            )
            Spacer(Modifier.height(Spacing.lg))
            Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(
                    IdeaEntity.CATEGORY_APP to "App",
                    IdeaEntity.CATEGORY_BUSINESS to "Business",
                    IdeaEntity.CATEGORY_CONTENT to "Content",
                    IdeaEntity.CATEGORY_SCHOOL to "School",
                    IdeaEntity.CATEGORY_PERSONAL to "Personal",
                    IdeaEntity.CATEGORY_OTHER to "Other"
                ).forEach { (v, label) ->
                    AppFilterChip(label = label, selected = category == v, onClick = { category = v })
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text("Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                listOf(
                    IdeaEntity.STATUS_NEW to "New",
                    IdeaEntity.STATUS_WORKING to "Working",
                    IdeaEntity.STATUS_COMPLETED to "Done"
                ).forEach { (v, label) ->
                    AppFilterChip(label = label, selected = status == v, onClick = { status = v })
                }
            }
            Spacer(Modifier.height(Spacing.xxl))
            AppPrimaryButton(
                text = if (saving) "Saving…" else "Save idea",
                onClick = {
                    if (title.isBlank()) { error = true; return@AppPrimaryButton }
                    saving = true
                    scope.launch {
                        if (ideaId == null) {
                            viewModel.ideaRepository.createIdea(title.trim(), body.trim(), category, status)
                        } else {
                            viewModel.ideaRepository.updateIdea(existing!!.copy(title = title.trim(), body = body.trim(), category = category, status = status, pinned = pinned))
                        }
                        onDone()
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.sm))
            AppTextButton(text = "Cancel", onClick = onCancel, modifier = Modifier.fillMaxWidth())
            if (ideaId != null) {
                Spacer(Modifier.height(Spacing.lg))
                AppTextButton(
                    text = "Delete idea",
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDelete && ideaId != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete idea?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.ideaRepository.deleteIdea(ideaId)
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
