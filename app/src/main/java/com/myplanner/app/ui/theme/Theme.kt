package com.myplanner.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun lightScheme(p: PaletteColors) = lightColorScheme(
    primary = p.primary,
    onPrimary = p.onPrimaryLight,
    primaryContainer = p.primaryContainerLight,
    onPrimaryContainer = p.primaryDark,
    secondary = p.secondary,
    onSecondary = Color.White,
    secondaryContainer = p.secondaryContainerLight,
    onSecondaryContainer = p.secondaryDeep,
    tertiary = p.primary,
    onTertiary = Color.White,
    tertiaryContainer = p.primaryContainerLight,
    onTertiaryContainer = p.primaryDark,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = LightSurfaceContainer,
    surfaceContainer = LightSurfaceVariant,
    surfaceContainerHigh = Color(0xFFE8EAF2),
    surfaceContainerHighest = Color(0xFFE0E3EC),
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = ScrimLight,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkOnSurface,
    inversePrimary = p.lightPrimary,
    surfaceTint = p.primary
)

private fun darkScheme(p: PaletteColors) = darkColorScheme(
    primary = p.lightPrimary,
    onPrimary = p.primaryDark,
    primaryContainer = p.primaryContainerDark,
    onPrimaryContainer = p.primaryContainerLight,
    secondary = p.lightSecondary,
    onSecondary = p.secondaryDeep,
    secondaryContainer = p.secondaryContainerDark,
    onSecondaryContainer = p.secondaryContainerLight,
    tertiary = p.lightPrimary,
    onTertiary = p.primaryDark,
    tertiaryContainer = p.primaryContainerDark,
    onTertiaryContainer = p.primaryContainerLight,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0A0C10),
    surfaceContainerLow = DarkSurfaceContainer,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = Color(0xFF2A2F3A),
    surfaceContainerHighest = Color(0xFF343A46),
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = ScrimDark,
    inverseSurface = LightSurface,
    inverseOnSurface = LightOnSurface,
    inversePrimary = p.primary,
    surfaceTint = p.lightPrimary
)

@Composable
fun MyPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: AppPalette = AppPalette.INDIGO,
    content: @Composable () -> Unit
) {
    val colors = palette.colors()
    val colorScheme = if (darkTheme) darkScheme(colors) else lightScheme(colors)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    CompositionLocalProvider(LocalSpacing provides AppSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MyPlannerTypography,
            shapes = MyPlannerShapes,
            content = content
        )
    }
}
