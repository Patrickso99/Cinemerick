package com.preichert.cinemerick.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Role-based colors of the Cinemerick redesign (docs/design, Main). Material's colorScheme is built from
 * these too, so M3 widgets inherit the same palette. Chain colors are not part of it: see [CinemaBrandColors].
 */
@Immutable
data class CinemerickColors(
    val background: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val outlineVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val scrim: Color,
    /** 1dp stroke around chain badges and chips in dark theme: makes the dark chains (Cinemazero, Cinergia, UCI) visible. */
    val chainEdge: Color,
)

val DarkCinemerickColors = CinemerickColors(
    background = Color(0xFF0D0B14),
    surface = Color(0xFF15121F),
    surfaceContainer = Color(0xFF24203D),
    surfaceContainerHigh = Color(0xFF2D2750),
    outlineVariant = Color(0xFF2E2946),
    onSurface = Color(0xFFECE9F7),
    onSurfaceVariant = Color(0xFFA9A3C2),
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF1A0F3D),
    primaryContainer = Color(0xFF3A2C7A),
    onPrimaryContainer = Color(0xFFE7DEFF),
    scrim = Color(0xA806040C),
    chainEdge = Color(0x47FFFFFF),
)

val LightCinemerickColors = CinemerickColors(
    background = Color(0xFFFAF8FF),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF3F0FC),
    surfaceContainerHigh = Color(0xFFEAE5F8),
    outlineVariant = Color(0xFFE2DCF3),
    onSurface = Color(0xFF1B1730),
    onSurfaceVariant = Color(0xFF5E5878),
    primary = Color(0xFF5B3FD6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7E0FF),
    onPrimaryContainer = Color(0xFF2A1670),
    scrim = Color(0x6B140C32),
    chainEdge = Color(0x00000000),
)

// Cinema brand colors: fixed in light and dark mode so each cinema is always recognizable.
// Values are the ones in use before the redesign; the redesign does not change them.
object CinemaBrandColors {
    val uciBlue = Color(0xFF1F3F7A)
    val onUciBlue = Color(0xFFFFFFFF)
    val theSpaceOrange = Color(0xFFE8650A)
    val onTheSpaceOrange = Color(0xFFFFFFFF)
    val notoriousSilver = Color(0xFFA4A5A6)
    val onNotoriousSilver = Color(0xFF1A1A1A)
    val cinergia = Color(0xFF414C4C)
    val onCinergia = Color(0xFFFFFFFF)
    val cristallo = Color(0xFFD22D27)
    val onCristallo = Color(0xFFFFFFFF)
    val donBosco = Color(0xFF8B3A3B)
    val onDonBosco = Color(0xFFFFFFFF)
    val cinemazero = Color(0xFF032129)
    val onCinemazero = Color(0xFFFFFFFF)
    val arcadia = Color(0xFF6E398A)
    val onArcadia = Color(0xFFFFFFFF)
}
