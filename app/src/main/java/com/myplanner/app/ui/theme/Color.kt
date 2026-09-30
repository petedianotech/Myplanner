package com.myplanner.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MyPlanner design tokens — four beautiful palettes.
 * Use semantic ColorScheme roles in UI; avoid hard-coding these elsewhere.
 */

enum class AppPalette(val id: String, val label: String, val description: String) {
    INDIGO("indigo", "Indigo", "Calm productivity · original"),
    OCEAN("ocean", "Ocean", "Cool blues and deep teal"),
    SUNSET("sunset", "Sunset", "Warm coral and amber"),
    FOREST("forest", "Forest", "Fresh greens and moss");

    companion object {
        fun fromId(id: String?): AppPalette =
            entries.firstOrNull { it.id == id } ?: INDIGO
    }
}

// Brand core (Indigo — default)
val IndigoPrimary = Color(0xFF4F46E5)
val IndigoDark = Color(0xFF3730A3)
val IndigoMuted = Color(0xFF6366F1)
val TealAccent = Color(0xFF14B8A6)
val TealDeep = Color(0xFF0F766E)

// Ocean
val OceanPrimary = Color(0xFF0284C7)
val OceanDark = Color(0xFF075985)
val OceanAccent = Color(0xFF06B6D4)
val OceanDeep = Color(0xFF0E7490)

// Sunset
val SunsetPrimary = Color(0xFFEA580C)
val SunsetDark = Color(0xFFC2410C)
val SunsetAccent = Color(0xFFF59E0B)
val SunsetDeep = Color(0xFFB45309)

// Forest
val ForestPrimary = Color(0xFF059669)
val ForestDark = Color(0xFF047857)
val ForestAccent = Color(0xFF34D399)
val ForestDeep = Color(0xFF065F46)

// Light surfaces
val LightBackground = Color(0xFFF8F9FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF0F6)
val LightSurfaceContainer = Color(0xFFF1F3F9)
val LightOnBackground = Color(0xFF171923)
val LightOnSurface = Color(0xFF171923)
val LightOnSurfaceVariant = Color(0xFF687083)
val LightOutline = Color(0xFFC5CAD6)
val LightOutlineVariant = Color(0xFFE2E5ED)

// Dark surfaces
val DarkBackground = Color(0xFF0D0F14)
val DarkSurface = Color(0xFF171A21)
val DarkSurfaceVariant = Color(0xFF22262F)
val DarkSurfaceContainer = Color(0xFF1C1F28)
val DarkOnBackground = Color(0xFFE8EAED)
val DarkOnSurface = Color(0xFFE8EAED)
val DarkOnSurfaceVariant = Color(0xFF9AA3B2)
val DarkOutline = Color(0xFF3A404C)
val DarkOutlineVariant = Color(0xFF2A2F3A)

// Status
val Success = Color(0xFF0D9488)
val SuccessContainer = Color(0xFFCCFBF1)
val OnSuccessContainer = Color(0xFF134E4A)
val Warning = Color(0xFFD97706)
val WarningContainer = Color(0xFFFEF3C7)
val OnWarningContainer = Color(0xFF78350F)
val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorLight = Color(0xFFFFFFFF)
val OnErrorDark = Color(0xFF690005)

// Gradient endpoints
val GradientStart = IndigoPrimary
val GradientEnd = TealAccent

val DisabledAlpha = 0.38f
val ScrimLight = Color.Black.copy(alpha = 0.32f)
val ScrimDark = Color.Black.copy(alpha = 0.5f)

data class PaletteColors(
    val primary: Color,
    val primaryDark: Color,
    val secondary: Color,
    val secondaryDeep: Color,
    val primaryContainerLight: Color,
    val primaryContainerDark: Color,
    val secondaryContainerLight: Color,
    val secondaryContainerDark: Color,
    val onPrimaryLight: Color = Color.White,
    val lightPrimary: Color,
    val lightSecondary: Color
)

fun AppPalette.colors(): PaletteColors = when (this) {
    AppPalette.INDIGO -> PaletteColors(
        primary = IndigoPrimary,
        primaryDark = IndigoDark,
        secondary = TealAccent,
        secondaryDeep = TealDeep,
        primaryContainerLight = Color(0xFFE0E7FF),
        primaryContainerDark = Color(0xFF3730A3),
        secondaryContainerLight = Color(0xFFCCFBF1),
        secondaryContainerDark = Color(0xFF0D9488),
        lightPrimary = Color(0xFFA5B4FC),
        lightSecondary = Color(0xFF5EEAD4)
    )
    AppPalette.OCEAN -> PaletteColors(
        primary = OceanPrimary,
        primaryDark = OceanDark,
        secondary = OceanAccent,
        secondaryDeep = OceanDeep,
        primaryContainerLight = Color(0xFFE0F2FE),
        primaryContainerDark = Color(0xFF075985),
        secondaryContainerLight = Color(0xFFCFFAFE),
        secondaryContainerDark = Color(0xFF0E7490),
        lightPrimary = Color(0xFF7DD3FC),
        lightSecondary = Color(0xFF67E8F9)
    )
    AppPalette.SUNSET -> PaletteColors(
        primary = SunsetPrimary,
        primaryDark = SunsetDark,
        secondary = SunsetAccent,
        secondaryDeep = SunsetDeep,
        primaryContainerLight = Color(0xFFFFEDD5),
        primaryContainerDark = Color(0xFF9A3412),
        secondaryContainerLight = Color(0xFFFEF3C7),
        secondaryContainerDark = Color(0xFFB45309),
        lightPrimary = Color(0xFFFB923C),
        lightSecondary = Color(0xFFFBBF24)
    )
    AppPalette.FOREST -> PaletteColors(
        primary = ForestPrimary,
        primaryDark = ForestDark,
        secondary = ForestAccent,
        secondaryDeep = ForestDeep,
        primaryContainerLight = Color(0xFFD1FAE5),
        primaryContainerDark = Color(0xFF065F46),
        secondaryContainerLight = Color(0xFFD1FAE5),
        secondaryContainerDark = Color(0xFF047857),
        lightPrimary = Color(0xFF6EE7B7),
        lightSecondary = Color(0xFFA7F3D0)
    )
}
