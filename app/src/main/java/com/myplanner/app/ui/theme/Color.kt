package com.myplanner.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MyPlanner design tokens — restrained indigo primary + teal accent.
 * Use semantic ColorScheme roles in UI; avoid hard-coding these elsewhere.
 */

// Brand core
val IndigoPrimary = Color(0xFF4F46E5)
val IndigoDark = Color(0xFF3730A3)
val IndigoMuted = Color(0xFF6366F1)
val TealAccent = Color(0xFF14B8A6)
val TealDeep = Color(0xFF0F766E)

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

// Dark surfaces (premium neutrals, not pure black)
val DarkBackground = Color(0xFF0D0F14)
val DarkSurface = Color(0xFF171A21)
val DarkSurfaceVariant = Color(0xFF22262F)
val DarkSurfaceContainer = Color(0xFF1C1F28)
val DarkOnBackground = Color(0xFFE8EAED)
val DarkOnSurface = Color(0xFFE8EAED)
val DarkOnSurfaceVariant = Color(0xFF9AA3B2)
val DarkOutline = Color(0xFF3A404C)
val DarkOutlineVariant = Color(0xFF2A2F3A)

// Status — use only when semantic meaning requires them
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

// Gradient endpoints (use sparingly — hero / selected moments only)
val GradientStart = IndigoPrimary
val GradientEnd = TealAccent

// Disabled / scrim helpers
val DisabledAlpha = 0.38f
val ScrimLight = Color.Black.copy(alpha = 0.32f)
val ScrimDark = Color.Black.copy(alpha = 0.5f)
