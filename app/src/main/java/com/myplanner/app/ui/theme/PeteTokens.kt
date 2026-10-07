package com.myplanner.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object PeteColors {
    val Midnight = Color(0xFF0A0B12)
    val DeepIndigo = Color(0xFF0B0E1A)
    val VioletDepth = Color(0xFF1A1030)
    val TealTint = Color(0xFF0D1A1F)
    val Indigo = Color(0xFF6366F1)
    val IndigoDeep = Color(0xFF4F46E5)
    val Violet = Color(0xFF8B5CF6)
    val Cyan = Color(0xFF22D3EE)
    val Teal = Color(0xFF14B8A6)
    val TealBright = Color(0xFF2DD4BF)
    val GlassFill = Color(0x1AFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val GlassFillStrong = Color(0x28FFFFFF)
    val OnDark = Color(0xFFF1F5F9)
    val OnDarkMuted = Color(0xFF94A3B8)
    val OnDarkSoft = Color(0xFFCBD5E1)
    val Amber = Color(0xFFFBBF24)
    val Rose = Color(0xFFFB7185)
}

object PeteGradients {
    val AmbientVertical: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                PeteColors.VioletDepth,
                PeteColors.DeepIndigo,
                PeteColors.Midnight,
                PeteColors.TealTint
            )
        )

    val AmbientRadial: Brush
        get() = Brush.radialGradient(
            colors = listOf(
                Color(0xFF2E1065).copy(alpha = 0.55f),
                PeteColors.DeepIndigo.copy(alpha = 0.3f),
                PeteColors.Midnight
            ),
            center = Offset(0.5f, 0.25f),
            radius = 1200f
        )

    val PillPrimary: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(PeteColors.IndigoDeep, PeteColors.Indigo, PeteColors.Teal)
        )

    val PillPrimaryVertical: Brush
        get() = Brush.verticalGradient(
            colors = listOf(PeteColors.Indigo, PeteColors.TealBright)
        )

    val OrbGlow: Brush
        get() = Brush.radialGradient(
            colors = listOf(
                PeteColors.Cyan.copy(alpha = 0.9f),
                PeteColors.Violet.copy(alpha = 0.5f),
                Color.Transparent
            )
        )

    val GlassBorderGradient: Brush
        get() = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.06f),
                PeteColors.Cyan.copy(alpha = 0.15f)
            )
        )
}
