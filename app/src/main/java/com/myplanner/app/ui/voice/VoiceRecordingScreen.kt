package com.myplanner.app.ui.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.myplanner.app.data.repository.VoiceNoteRepository
import com.myplanner.app.data.voice.VoiceAudioStorage
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppSecondaryButton
import com.myplanner.app.ui.components.AppTextButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.components.PageHeader
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private enum class RecState { Idle, Recording, Paused, Stopped }

@Composable
fun VoiceRecordingScreen(
    onSave: suspend (title: String, filePath: String, durationMillis: Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { VoiceAudioStorage(context) }
    var title by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(RecState.Idle) }
    var duration by remember { mutableLongStateOf(0L) }
    var filePath by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showDenied by remember { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startRec(context, storage) { result ->
            result.onSuccess { (rec, path) ->
                recorder = rec; filePath = path; state = RecState.Recording; duration = 0L; errorMessage = null
            }.onFailure {
                errorMessage = "Could not start recording. Check storage space and try again."
            }
        } else showDenied = true
    }

    fun releaseRecorder(stop: Boolean) {
        val rec = recorder ?: return
        if (stop) runCatching { rec.stop() }
        runCatching { rec.release() }
        recorder = null
    }

    fun stopRecording() {
        if (state == RecState.Recording || state == RecState.Paused) {
            releaseRecorder(stop = true)
            state = RecState.Stopped
        }
    }

    fun discardAndLeave() {
        releaseRecorder(stop = state == RecState.Recording || state == RecState.Paused)
        storage.deleteIfExists(filePath)
        filePath = ""
        onCancel()
    }

    fun attemptLeave() {
        when (state) {
            RecState.Recording, RecState.Paused -> showDiscard = true
            RecState.Stopped -> if (duration > 0L && filePath.isNotBlank()) showDiscard = true else discardAndLeave()
            RecState.Idle -> onCancel()
        }
    }

    BackHandler { attemptLeave() }
    DisposableEffect(Unit) { onDispose { releaseRecorder(stop = false) } }

    LaunchedEffect(state) {
        if (state == RecState.Recording) {
            val base = duration
            val start = System.currentTimeMillis()
            while (isActive && state == RecState.Recording) {
                duration = base + (System.currentTimeMillis() - start)
                delay(200)
            }
        }
    }

    fun startOrResume() {
        when (state) {
            RecState.Idle -> {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) startRec(context, storage) { result ->
                    result.onSuccess { (rec, path) ->
                        recorder = rec; filePath = path; state = RecState.Recording; duration = 0L; errorMessage = null
                    }.onFailure {
                        errorMessage = "Could not start recording. Check storage space and try again."
                    }
                } else showPermissionRationale = true
            }
            RecState.Paused -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    runCatching { recorder?.resume() }
                        .onSuccess { state = RecState.Recording }
                        .onFailure { errorMessage = "Could not resume recording." }
                }
            }
            else -> Unit
        }
    }

    fun pause() {
        if (state == RecState.Recording && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching { recorder?.pause() }.onSuccess { state = RecState.Paused }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner)
                .padding(horizontal = Spacing.screenHorizontal).padding(bottom = Spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageHeader(title = "Voice note", subtitle = "Recorded and stored only on this device")
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title (optional)",
                placeholder = VoiceNoteRepository.defaultTitle(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.xxl))
            RecordingIndicator(active = state == RecState.Recording)
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { contentDescription = "Elapsed time ${formatDuration(duration)}" }
            )
            Text(
                text = when (state) {
                    RecState.Idle -> "Tap Record to start"
                    RecState.Recording -> "Recording…"
                    RecState.Paused -> "Paused"
                    RecState.Stopped -> "Ready to save"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            errorMessage?.let {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
            when (state) {
                RecState.Idle -> AppPrimaryButton(text = "Record", onClick = { startOrResume() }, leadingIcon = Icons.Outlined.Mic, modifier = Modifier.fillMaxWidth())
                RecState.Recording -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        AppSecondaryButton(text = "Pause", onClick = { pause() }, modifier = Modifier.weight(1f))
                    }
                    AppPrimaryButton(text = "Stop", onClick = { stopRecording() }, modifier = Modifier.weight(1f))
                }
                RecState.Paused -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AppSecondaryButton(text = "Resume", onClick = { startOrResume() }, modifier = Modifier.weight(1f))
                    AppPrimaryButton(text = "Stop", onClick = { stopRecording() }, modifier = Modifier.weight(1f))
                }
                RecState.Stopped -> {
                    AppPrimaryButton(
                        text = if (saving) "Saving…" else "Save voice note",
                        onClick = {
                            scope.launch {
                                saving = true
                                try {
                                    onSave(title.ifBlank { VoiceNoteRepository.defaultTitle() }, filePath, duration)
                                } finally { saving = false }
                            }
                        },
                        enabled = !saving && duration > 0L && filePath.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    AppSecondaryButton(
                        text = "Record again",
                        onClick = {
                            storage.deleteIfExists(filePath)
                            filePath = ""; duration = 0L; state = RecState.Idle; errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppTextButton(text = "Cancel", onClick = { attemptLeave() }, enabled = !saving)
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Microphone access") },
            text = { Text("MyPlanner needs the microphone only while you record a voice note. Audio stays on this device and is never uploaded.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showPermissionRationale = false }) { Text("Not now") } }
        )
    }
    if (showDenied) {
        AlertDialog(
            onDismissRequest = { showDenied = false },
            title = { Text("Microphone blocked") },
            text = { Text("Recording needs microphone permission. You can enable it in system settings. The rest of MyPlanner still works offline without it.") },
            confirmButton = {
                TextButton(onClick = {
                    showDenied = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                }) { Text("Open settings") }
            },
            dismissButton = { TextButton(onClick = { showDenied = false }) { Text("Close") } }
        )
    }
    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Discard this recording?") },
            text = { Text("The audio will be deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = { showDiscard = false; discardAndLeave() }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Keep recording") } }
        )
    }
}

@Composable
private fun RecordingIndicator(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "rec")
    val pulse by transition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val scale = if (active) pulse else 1f
    Box(
        modifier = Modifier.size(96.dp).scale(scale).clip(CircleShape)
            .background((if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                .copy(alpha = if (active) 0.18f else 1f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (active) Icons.Outlined.Stop else Icons.Outlined.Mic,
                contentDescription = null,
                tint = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun startRec(
    context: android.content.Context,
    storage: VoiceAudioStorage,
    onResult: (Result<Pair<MediaRecorder, String>>) -> Unit
) {
    val file = storage.newRecordingFile()
    val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
    else @Suppress("DEPRECATION") MediaRecorder()
    try {
        rec.setAudioSource(MediaRecorder.AudioSource.MIC)
        rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        rec.setAudioEncodingBitRate(128_000)
        rec.setAudioSamplingRate(44_100)
        rec.setOutputFile(file.absolutePath)
        rec.prepare()
        rec.start()
        onResult(Result.success(rec to file.absolutePath))
    } catch (e: Exception) {
        runCatching { rec.release() }
        runCatching { if (file.exists()) file.delete() }
        onResult(Result.failure(e))
    }
}

internal fun formatDuration(millis: Long): String {
    val totalSec = (millis / 1000).coerceAtLeast(0).toInt()
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
