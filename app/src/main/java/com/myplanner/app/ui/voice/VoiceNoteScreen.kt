package com.myplanner.app.ui.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppSecondaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VoiceNoteScreen(
    onSave: suspend (title: String, filePath: String, durationMillis: Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var recording by remember { mutableStateOf(false) }
    var duration by remember { mutableLongStateOf(0L) }
    var filePath by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRecording(context) { rec, path ->
            recorder = rec; filePath = path; recording = true; duration = 0L
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { recorder?.stop() }
            recorder?.release()
        }
    }

    LaunchedEffect(recording) {
        if (recording) {
            val start = System.currentTimeMillis() - duration
            while (isActive && recording) {
                duration = System.currentTimeMillis() - start
                delay(200)
            }
        }
    }

    fun toggleRecord() {
        if (recording) {
            runCatching { recorder?.stop() }
            recorder?.release()
            recorder = null
            recording = false
        } else {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                startRecording(context) { rec, path ->
                    recorder = rec; filePath = path; recording = true; duration = 0L
                }
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = Spacing.screenHorizontal).padding(bottom = Spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageHeader(title = "Voice note", subtitle = "Record a short memo. Saved only on this device.")
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppTextField(value = title, onValueChange = { title = it }, label = "Title", placeholder = "e.g. Meeting thoughts", singleLine = true)
            Spacer(modifier = Modifier.height(Spacing.xxl))
            Icon(
                imageVector = if (recording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                contentDescription = if (recording) "Stop recording" else "Start recording",
                tint = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(text = formatDuration(duration), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = if (recording) "Recording…" else "Tap to start or stop",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            AppSecondaryButton(text = if (recording) "Stop" else "Record", onClick = { toggleRecord() }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(Spacing.md))
            AppPrimaryButton(
                text = if (saving) "Saving…" else "Save voice note",
                onClick = {
                    scope.launch {
                        saving = true
                        try {
                            if (recording) {
                                runCatching { recorder?.stop() }
                                recorder?.release()
                                recorder = null
                                recording = false
                            }
                            onSave(title.ifBlank { "Voice note" }, filePath, duration)
                        } finally { saving = false }
                    }
                },
                enabled = !saving && duration > 0L,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppTextButton(text = "Cancel", onClick = onCancel, enabled = !saving)
        }
    }
}

private fun startRecording(context: android.content.Context, onStarted: (MediaRecorder, String) -> Unit) {
    val file = File(context.filesDir, "voice_${System.currentTimeMillis()}.m4a")
    val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else {
        @Suppress("DEPRECATION") MediaRecorder()
    }
    try {
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        onStarted(rec, file.absolutePath)
    } catch (_: Exception) {
        rec.release()
        onStarted(rec, "")
    }
}

private fun formatDuration(millis: Long): String {
    val totalSec = (millis / 1000).toInt()
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
