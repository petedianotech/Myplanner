package com.myplanner.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.myplanner.app.ui.capture.CaptureType
import com.myplanner.app.ui.theme.AppMotion
import com.myplanner.app.ui.theme.AppShapes
import com.myplanner.app.ui.theme.Spacing

@Composable
fun SpeedDialFab(
    onSelect: (CaptureType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = AppMotion.mediumTween(),
        label = "fabRotate"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(AppMotion.shortTween()) + scaleIn(AppMotion.shortTween(), initialScale = 0.8f),
            exit = fadeOut(AppMotion.shortTween()) + scaleOut(AppMotion.shortTween(), targetScale = 0.8f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedDialItem(Icons.Outlined.TaskAlt, "Task", CaptureType.TASK, onSelect) { expanded = false }
                SpeedDialItem(Icons.Outlined.Alarm, "Reminder", CaptureType.REMINDER, onSelect) { expanded = false }
                SpeedDialItem(Icons.Outlined.Notes, "Note", CaptureType.NOTE, onSelect) { expanded = false }
                SpeedDialItem(Icons.Outlined.Lightbulb, "Idea", CaptureType.IDEA, onSelect) { expanded = false }
                SpeedDialItem(Icons.Outlined.Mic, "Voice", CaptureType.VOICE, onSelect) { expanded = false }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = AppShapes.button,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.Close else Icons.Outlined.Add,
                contentDescription = if (expanded) "Close" else "Quick capture",
                modifier = Modifier.rotate(if (expanded) 0f else rotation)
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    icon: ImageVector,
    label: String,
    type: CaptureType,
    onSelect: (CaptureType) -> Unit,
    onDone: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
        SmallFloatingActionButton(
            onClick = {
                onDone()
                onSelect(type)
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = AppShapes.button
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}
