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
    /** Something unavailable or gone wrong, e.g. the "sold out" badge. Material's error color. */
    val error: Color,
    val onError: Color,
    /** Error text and outlines on the app surfaces: [error] is too dark to read on the dark background. */
    val errorText: Color,
    /** Something to check before going on, e.g. a cinema the assistant is not sure about. */
    val warningContainer: Color,
    val onWarningContainer: Color,
    /** 1dp stroke around chain badges and chips in dark theme: makes the dark chains (Cinemazero, Cinergia, UCI) visible. */
    val chainEdge: Color,
    /** Container of a control drawn over a picture (e.g. the close button on a backdrop), the same in both themes. */
    val overImageContainer: Color,
    /** Content of a control drawn over a picture. */
    val onOverImage: Color,
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
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorText = Color(0xFFF2B8B5),
    warningContainer = Color(0xFF5C3F06),
    onWarningContainer = Color(0xFFFDF0D0),
    chainEdge = Color(0x47FFFFFF),
    overImageContainer = Color(0x8C0A0812),
    onOverImage = Color(0xFFFFFFFF),
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
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorText = Color(0xFFB3261E),
    warningContainer = Color(0xFFFDF0D0),
    onWarningContainer = Color(0xFF3D2A04),
    chainEdge = Color(0x00000000),
    overImageContainer = Color(0x8C0A0812),
    onOverImage = Color(0xFFFFFFFF),
)

// Cinema brand colors: fixed in light and dark mode so each cinema is always recognizable.
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

// Colors of the app's own mark (the splash screen): fixed in light and dark mode, like a logo.
object CinemerickBrandColors {
    val play = Color(0xFF4B2E83)
    val dash = Color(0xFF9C8CC8)
    val italyGreen = Color(0xFF009246)
    val italyRed = Color(0xFFCE2B37)
}
