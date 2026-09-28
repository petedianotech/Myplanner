package com.myplanner.app.ui.note

import androidx.compose.runtime.Composable
import com.myplanner.app.ui.capture.CaptureFormScreen

@Composable
fun CreateNoteScreen(
    onSave: suspend (title: String, body: String) -> Unit,
    onCancel: () -> Unit
) {
    CaptureFormScreen(
        title = "New note",
        subtitle = "Saved on this device.",
        primaryLabel = "Save note",
        fieldLabel = "Title",
        fieldPlaceholder = "Give this note a name",
        notesLabel = "Note",
        notesPlaceholder = "Write freely…",
        onSave = onSave,
        onCancel = onCancel
    )
}
