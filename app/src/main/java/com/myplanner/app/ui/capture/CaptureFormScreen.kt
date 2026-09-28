package com.myplanner.app.ui.capture

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun CaptureFormScreen(
    title: String,
    subtitle: String,
    primaryLabel: String,
    fieldLabel: String,
    fieldPlaceholder: String,
    notesLabel: String? = "Notes (optional)",
    notesPlaceholder: String = "Extra details…",
    extraContent: @Composable (() -> Unit)? = null,
    onSave: suspend (title: String, notes: String) -> Unit,
    onCancel: () -> Unit
) {
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxxl)
        ) {
            PageHeader(title = title, subtitle = subtitle)
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppTextField(
                value = value,
                onValueChange = {
                    value = it
                    error = false
                },
                label = fieldLabel,
                placeholder = fieldPlaceholder,
                isError = error,
                supportingText = if (error) "Please enter a title" else null,
                singleLine = true
            )
            if (notesLabel != null) {
                Spacer(modifier = Modifier.height(Spacing.md))
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = notesLabel,
                    placeholder = notesPlaceholder,
                    singleLine = false,
                    maxLines = 6
                )
            }
            extraContent?.invoke()
            Spacer(modifier = Modifier.height(Spacing.section))
            AppPrimaryButton(
                text = if (saving) "Saving…" else primaryLabel,
                onClick = {
                    if (value.isBlank()) {
                        error = true
                        return@AppPrimaryButton
                    }
                    scope.launch {
                        saving = true
                        try {
                            onSave(value.trim(), notes.trim())
                        } finally {
                            saving = false
                        }
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppTextButton(
                text = "Cancel",
                onClick = onCancel,
                enabled = !saving
            )
        }
    }
}
