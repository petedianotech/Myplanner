package com.myplanner.app.ui.pete

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myplanner.app.ui.components.PeteAmbientBackground
import com.myplanner.app.ui.components.PeteGlassCard
import com.myplanner.app.ui.components.PeteGlassPill
import com.myplanner.app.ui.components.PeteOrb
import com.myplanner.app.ui.theme.AppShapes
import com.myplanner.app.ui.theme.PeteColors

@Composable
fun ConversationScreen(
    onBack: () -> Unit,
    onNavigateBrief: (() -> Unit)? = null,
    onNavigateFocus: ((Int) -> Unit)? = null,
    onNavigateCommand: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.tapListen() }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }
    LaunchedEffect(state.navigateHint, state.focusMinutes) {
        when (state.navigateHint) {
            "brief" -> onNavigateBrief?.invoke()
            "focus" -> onNavigateFocus?.invoke(state.focusMinutes ?: 25)
            "command" -> onNavigateCommand?.invoke()
        }
        if (state.navigateHint != null) viewModel.clearNavigateHint()
    }

    Box(modifier = modifier.fillMaxSize()) {
        PeteAmbientBackground()
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeteGlassPill(text = "Back", onClick = onBack)
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        when (state.listenState) {
                            PeteListenState.Listening -> "Listening…"
                            PeteListenState.Thinking -> "Thinking…"
                            PeteListenState.Speaking -> "Speaking…"
                            else -> "Pete"
                        },
                        color = PeteColors.OnDarkMuted,
                        fontSize = 13.sp
                    )
                    Text(state.statusHint, color = PeteColors.Cyan, fontSize = 10.sp)
                }
            }

            Box(Modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PeteOrb(
                    size = 96.dp,
                    glowing = state.listenState == PeteListenState.Listening ||
                        state.listenState == PeteListenState.Speaking
                )
            }
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.fromPete) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        PeteGlassCard(
                            modifier = Modifier.fillMaxWidth(0.88f),
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Text(
                                msg.text,
                                color = PeteColors.OnDark,
                                fontSize = 15.sp,
                                lineHeight = 21.sp
                            )
                        }
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeteGlassPill(text = "Today", onClick = { viewModel.quickAction("Today") })
                PeteGlassPill(text = "Add task", onClick = { viewModel.quickAction("Add task") })
                PeteGlassPill(text = "Focus 25m", onClick = { viewModel.quickAction("Focus 25m") })
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(AppShapes.textField)
                        .background(PeteColors.GlassFillStrong)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    if (state.input.isEmpty()) {
                        Text("Type a message…", color = PeteColors.OnDarkMuted, fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = state.input,
                        onValueChange = viewModel::onInputChange,
                        textStyle = TextStyle(color = PeteColors.OnDark, fontSize = 15.sp),
                        cursorBrush = SolidColor(PeteColors.Cyan),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) viewModel.tapListen()
                        else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.listenState == PeteListenState.Listening)
                                PeteColors.Teal else PeteColors.IndigoDeep
                        )
                ) {
                    Icon(
                        if (state.listenState == PeteListenState.Listening)
                            Icons.Outlined.MicOff else Icons.Outlined.Mic,
                        contentDescription = "Mic",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { viewModel.sendText() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PeteColors.IndigoDeep)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
