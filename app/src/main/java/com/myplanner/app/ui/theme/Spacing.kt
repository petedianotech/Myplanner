package com.myplanner.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Consistent spacing scale used across screens and components.
 */
data class AppSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val huge: Dp = 40.dp,
    val screenHorizontal: Dp = 20.dp,
    val screenVertical: Dp = 16.dp,
    val section: Dp = 24.dp,
    val cardPadding: Dp = 16.dp
)

val LocalSpacing = staticCompositionLocalOf { AppSpacing() }

object AppDimens {
    val iconSm = 18.dp
    val iconMd = 24.dp
    val iconLg = 32.dp
    val minTouchTarget = 48.dp
    val topBarHeight = 64.dp
    val bottomNavHeight = 80.dp
    val cardElevation = 1.dp
    val cardElevationRaised = 3.dp
}

val Spacing: AppSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
