package com.myplanner.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplanner.app.ui.theme.AppShapes
import com.myplanner.app.ui.theme.PeteColors
import com.myplanner.app.ui.theme.PeteGradients

@Composable
fun PeteAmbientBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(PeteGradients.AmbientVertical))
        Box(Modifier.fillMaxSize().background(PeteGradients.AmbientRadial))
    }
}

@Composable
fun PeteGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(AppShapes.glass)
            .background(PeteColors.GlassFillStrong)
            .border(1.dp, PeteColors.GlassBorder, AppShapes.glass)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun PetePillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = PeteGradients.PillPrimary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 36.dp, vertical = 16.dp)
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(AppShapes.pill)
            .background(if (enabled) gradient else SolidColor(Color.Gray.copy(alpha = 0.4f)))
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick
            )
            .padding(contentPadding)
            .defaultMinSize(minHeight = 52.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
fun PeteGlassPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(AppShapes.pill)
            .background(PeteColors.GlassFill)
            .border(1.dp, PeteColors.GlassBorder, AppShapes.pill)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = PeteColors.OnDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PeteOrb(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    glowing: Boolean = false
) {
    Box(
        modifier = modifier
            .defaultMinSize(size, size)
            .clip(CircleShape)
            .background(
                if (glowing) PeteGradients.OrbGlow
                else Brush.radialGradient(
                    listOf(
                        PeteColors.Indigo.copy(alpha = 0.7f),
                        PeteColors.Violet.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        PeteColors.Cyan.copy(alpha = 0.2f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {}
}
