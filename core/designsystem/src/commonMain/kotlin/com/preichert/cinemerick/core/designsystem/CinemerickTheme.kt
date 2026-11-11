package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The Cinemerick role colors for the current theme. Read them with [CinemerickTheme.colors]. */
val LocalCinemerickColors = staticCompositionLocalOf { DarkCinemerickColors }

@Composable
fun CinemerickTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) DarkCinemerickColors else LightCinemerickColors
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            background = colors.background,
            onBackground = colors.onSurface,
            surface = colors.surface,
            onSurface = colors.onSurface,
            onSurfaceVariant = colors.onSurfaceVariant,
            surfaceVariant = colors.surfaceContainer,
            surfaceContainerLowest = colors.background,
            surfaceContainerLow = colors.surface,
            surfaceContainer = colors.surfaceContainer,
            surfaceContainerHigh = colors.surfaceContainerHigh,
            surfaceContainerHighest = colors.surfaceContainerHigh,
            outline = colors.onSurfaceVariant,
            outlineVariant = colors.outlineVariant,
            scrim = colors.scrim,
            // Not part of the redesign palette: Material defaults for error states.
            error = Color(0xFFF2B8B5),
            onError = Color(0xFF601410),
            errorContainer = Color(0xFF8C1D18),
            onErrorContainer = Color(0xFFF9DEDC),
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            background = colors.background,
            onBackground = colors.onSurface,
            surface = colors.surface,
            onSurface = colors.onSurface,
            onSurfaceVariant = colors.onSurfaceVariant,
            surfaceVariant = colors.surfaceContainer,
            surfaceContainerLowest = colors.surface,
            surfaceContainerLow = colors.background,
            surfaceContainer = colors.surfaceContainer,
            surfaceContainerHigh = colors.surfaceContainerHigh,
            surfaceContainerHighest = colors.surfaceContainerHigh,
            outline = colors.onSurfaceVariant,
            outlineVariant = colors.outlineVariant,
            scrim = colors.scrim,
            error = Color(0xFFB3261E),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFF9DEDC),
            onErrorContainer = Color(0xFF410E0B),
        )
    }

    CompositionLocalProvider(LocalCinemerickColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = cinemerickTypography(),
            shapes = CinemerickShapes,
        ) {
            content()
        }
    }
}

/**
 * Entry point for the design tokens, as in the redesign spec:
 * `CinemerickTheme.colors`, `CinemerickTheme.spacing`, `CinemerickTheme.chain`.
 */
object CinemerickTheme {
    val colors: CinemerickColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCinemerickColors.current

    val spacing: Spacing = Spacing

    val chain: CinemaBrandColors = CinemaBrandColors
}
