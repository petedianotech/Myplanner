package com.myplanner.app.ui.task

import androidx.compose.runtime.Composable
import com.myplanner.app.ui.capture.CaptureFormScreen

@Composable
fun CreateTaskScreen(
    onSave: suspend (title: String, notes: String) -> Unit,
    onCancel: () -> Unit
) {
    CaptureFormScreen(
        title = "New task",
        subtitle = "Capture what needs doing. You can refine it later.",
        primaryLabel = "Save task",
        fieldLabel = "Task",
        fieldPlaceholder = "e.g. Draft the weekly plan",
        onSave = onSave,
        onCancel = onCancel
    )
}
