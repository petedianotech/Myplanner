package com.myplanner.app.ui.voice

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.AppPrimaryButton
import com.myplanner.app.ui.components.AppSecondaryButton
import com.myplanner.app.ui.components.AppTextField
import com.myplanner.app.ui.home.HomeViewModel
import com.myplanner.app.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceNotePlayerScreen(
    noteId: Long,
    onDone: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val note by viewModel.voiceNoteRepository.observeVoiceNote(noteId)
        .collectAsStateWithLifecycle(null)
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableFloatStateOf(0f) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var playError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(note) {
        val n = note ?: return@LaunchedEffect
        if (!loaded) {
            title = n.title
            durationMs = n.durationMillis.toFloat().coerceAtLeast(1f)
            loaded = true
        }
    }

    fun releasePlayer() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        playing = false
    }

    fun prepareAndPlay(path: String) {
        releasePlayer()
        val file = File(path)
        if (!file.exists()) {
            playError = "Audio file is missing on this device."
            return
        }
        try {
            val mp = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    playing = false
                    position = durationMs
                }
            }
            durationMs = mp.duration.toFloat().coerceAtLeast(1f)
            mp.start()
            player = mp
            playing = true
            playError = null
        } catch (_: Exception) {
            playError = "Could not play this recording."
            releasePlayer()
        }
    }

    DisposableEffect(Unit) {
        onDispose { releasePlayer() }
    }

    LaunchedEffect(playing) {
        while (isActive && playing) {
            val p = player
            if (p != null) position = p.currentPosition.toFloat()
            delay(200)
        }
    }

    BackHandler {
        releasePlayer()
        onDone()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Voice note", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        releasePlayer()
                        onDone()
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = note?.let { formatVoiceDate(it.createdAtEpochMillis) } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xxl))
            Text(
                text = "${formatDuration(position.toLong())} / ${formatDuration(durationMs.toLong())}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Slider(
                value = position.coerceIn(0f, durationMs),
                onValueChange = { newPos ->
                    position = newPos
                    player?.seekTo(newPos.toInt())
                },
                valueRange = 0f..durationMs.coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            playError?.let {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppPrimaryButton(
                text = if (playing) "Pause" else "Play",
                onClick = {
                    val path = note?.filePath.orEmpty()
                    if (playing) {
                        player?.pause()
                        playing = false
                    } else if (player != null) {
                        player?.start()
                        playing = true
                    } else {
                        prepareAndPlay(path)
                    }
                },
                leadingIcon = if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                modifier = Modifier.fillMaxWidth(),
                enabled = note != null
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            AppSecondaryButton(
                text = "Save title",
                onClick = { scope.launch { viewModel.voiceNoteRepository.rename(noteId, title) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            )
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this voice note?") },
            text = { Text("The recording will be removed from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        releasePlayer()
                        viewModel.voiceNoteRepository.deleteVoiceNote(noteId)
                        showDelete = false
                        onDone()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Keep") }
            }
        )
    }
}
