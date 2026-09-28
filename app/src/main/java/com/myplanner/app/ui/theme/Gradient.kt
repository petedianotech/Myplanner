package com.myplanner.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Brand gradient utilities. Use only for hero areas, selected moments,
 * or important summary surfaces — never as a default for cards/buttons.
 */
object AppGradients {
    val brandColors = listOf(GradientStart, GradientEnd)

    fun brandLinear(
        start: Offset = Offset.Zero,
        end: Offset = Offset.Infinite
    ): Brush = Brush.linearGradient(
        colors = brandColors,
        start = start,
        end = end
    )

    fun brandHorizontal(): Brush = Brush.horizontalGradient(brandColors)

    fun brandVertical(): Brush = Brush.verticalGradient(brandColors)

    /** Soft tint for subtle surfaces (low-opacity brand wash). */
    fun softTint(base: Color = GradientStart, alpha: Float = 0.08f): Color =
        base.copy(alpha = alpha)
}

@Composable
fun rememberBrandGradient(): Brush = remember {
    AppGradients.brandHorizontal()
}
