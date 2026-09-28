package com.myplanner.app.ui.reminder

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.myplanner.app.R
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.theme.MyPlannerTheme
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun CreateFirstReminderScreen(
    onSave: suspend (title: String, notes: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val canSave = title.isNotBlank() && !isSaving

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            PageHeader(
                title = stringResource(R.string.create_reminder_title),
                subtitle = stringResource(R.string.create_reminder_subtitle)
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = {
                    title = it
                    showError = false
                },
                label = stringResource(R.string.create_reminder_label),
                placeholder = stringResource(R.string.create_reminder_placeholder),
                isError = showError,
                supportingText = if (showError) {
                    stringResource(R.string.create_reminder_error)
                } else {
                    stringResource(R.string.create_reminder_hint)
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            AppTextField(
                value = notes,
                onValueChange = { notes = it },
                label = stringResource(R.string.create_reminder_notes_label),
                placeholder = stringResource(R.string.create_reminder_notes_placeholder),
                singleLine = false,
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(Spacing.section))
            AppPrimaryButton(
                text = if (isSaving) {
                    stringResource(R.string.create_reminder_saving)
                } else {
                    stringResource(R.string.create_reminder_save)
                },
                onClick = {
                    if (title.isBlank()) {
                        showError = true
                        return@AppPrimaryButton
                    }
                    scope.launch {
                        isSaving = true
                        try {
                            onSave(title.trim(), notes.trim())
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppTextButton(
                text = stringResource(R.string.create_reminder_cancel),
                onClick = onCancel,
                enabled = !isSaving
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateFirstReminderPreview() {
    MyPlannerTheme {
        CreateFirstReminderScreen(onSave = { _, _ -> }, onCancel = {})
    }
}
