package com.myplanner.app.ui.pete

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ai.GeminiConfig
import com.myplanner.app.ui.components.PeteAmbientBackground
import com.myplanner.app.ui.components.PeteGlassPill
import com.myplanner.app.ui.components.PetePillButton
import com.myplanner.app.ui.theme.AppShapes
import com.myplanner.app.ui.theme.PeteColors
import kotlin.math.sin

@Composable
fun LiveCallScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveCallViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startCall()
    }

    Box(modifier = modifier.fillMaxSize()) {
        PeteAmbientBackground()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeteGlassPill(text = "Back", onClick = {
                    viewModel.endCall()
                    onBack()
                })
                Spacer(Modifier.weight(1f))
                Text(
                    "Live call",
                    color = PeteColors.OnDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            Text(
                state.status,
                color = PeteColors.Cyan,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeminiConfig.liveModels.forEach { opt ->
                    val selected = opt.id == state.selectedLiveModel
                    val enabled = state.phase == LiveCallPhase.Idle || state.phase == LiveCallPhase.Error
                    Box(
                        modifier = Modifier
                            .clip(AppShapes.pill)
                            .background(if (selected) PeteColors.IndigoDeep else PeteColors.GlassFill)
                            .border(
                                1.dp,
                                if (selected) PeteColors.Cyan else PeteColors.GlassBorder,
                                AppShapes.pill
                            )
                            .clickable(enabled = enabled) { viewModel.selectLiveModel(opt.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            opt.label,
                            color = if (selected) Color.White else PeteColors.OnDarkSoft,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceVisualizer(
                    label = "You",
                    level = state.userLevel,
                    accent = PeteColors.TealBright,
                    active = state.phase == LiveCallPhase.Live
                )
                VoiceVisualizer(
                    label = "Pete",
                    level = state.peteLevel,
                    accent = PeteColors.Violet,
                    active = state.phase == LiveCallPhase.Live
                )
            }

            Spacer(Modifier.height(28.dp))

            TranscriptCard(title = "You", body = state.userTranscript)
            Spacer(Modifier.height(10.dp))
            TranscriptCard(title = "Pete", body = state.peteTranscript)

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = PeteColors.Rose, fontSize = 13.sp)
            }

            Spacer(Modifier.weight(1f))

            when (state.phase) {
                LiveCallPhase.Idle, LiveCallPhase.Error -> {
                    PetePillButton(
                        text = "Start live call",
                        onClick = {
                            val ok = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (ok) viewModel.startCall()
                            else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LiveCallPhase.Connecting -> {
                    PetePillButton(
                        text = "Connecting…",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LiveCallPhase.Live, LiveCallPhase.Ending -> {
                    PetePillButton(
                        text = "End call",
                        onClick = { viewModel.endCall("Call ended") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Say stop / goodbye to hang up · start call / hey peter to begin",
                color = PeteColors.OnDarkMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TranscriptCard(title: String, body: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(AppShapes.glass)
            .background(PeteColors.GlassFillStrong)
            .border(1.dp, PeteColors.GlassBorder, AppShapes.glass)
            .padding(14.dp)
    ) {
        Text(title, color = PeteColors.OnDarkMuted, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            body.ifBlank { "…" },
            color = PeteColors.OnDark,
            fontSize = 14.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VoiceVisualizer(
    label: String,
    level: Float,
    accent: Color,
    active: Boolean
) {
    val infinite = rememberInfiniteTransition(label = "viz")
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(alpha = 0.35f + level * 0.45f),
                            Color.Transparent
                        )
                    )
                )
                .border(1.5.dp, accent.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(110.dp)) {
                val bars = 16
                val barW = size.width / (bars * 1.8f)
                val mid = size.height / 2f
                for (i in 0 until bars) {
                    val t = i / bars.toFloat()
                    val wave = if (active) {
                        (0.25f + level * 0.75f) * (0.55f + 0.45f * sin((pulse * 6.28f) + t * 8f))
                    } else 0.12f
                    val h = size.height * wave.coerceIn(0.08f, 1f)
                    val x = t * size.width
                    drawRoundRect(
                        color = accent.copy(alpha = 0.85f),
                        topLeft = Offset(x, mid - h / 2f),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = PeteColors.OnDarkSoft, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
