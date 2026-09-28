package com.myplanner.app.ui.idea

import androidx.compose.runtime.Composable
import com.myplanner.app.ui.capture.CaptureFormScreen

@Composable
fun CreateIdeaScreen(
    onSave: suspend (title: String, body: String) -> Unit,
    onCancel: () -> Unit
) {
    CaptureFormScreen(
        title = "New idea",
        subtitle = "Catch it before it fades.",
        primaryLabel = "Save idea",
        fieldLabel = "Idea",
        fieldPlaceholder = "What is the thought?",
        notesLabel = "Details (optional)",
        notesPlaceholder = "Context, next steps…",
        onSave = onSave,
        onCancel = onCancel
    )
}
