package com.myplanner.app.ui.voice

import androidx.compose.runtime.Composable

/** Entry used by Quick Capture — opens the full recording experience. */
@Composable
fun VoiceNoteScreen(
    onSave: suspend (title: String, filePath: String, durationMillis: Long) -> Unit,
    onCancel: () -> Unit
) {
    VoiceRecordingScreen(onSave = onSave, onCancel = onCancel)
}
