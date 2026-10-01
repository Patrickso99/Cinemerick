package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun CinemerickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = CinemerickColors.darkPrimary,
            onPrimary = CinemerickColors.darkOnPrimary,
            primaryContainer = CinemerickColors.darkPrimaryContainer,
            onPrimaryContainer = CinemerickColors.darkOnPrimaryContainer,
            secondary = CinemerickColors.darkSecondary,
            onSecondary = CinemerickColors.darkOnSecondary,
            secondaryContainer = CinemerickColors.darkSecondaryContainer,
            onSecondaryContainer = CinemerickColors.darkOnSecondaryContainer,
            tertiary = CinemerickColors.darkTertiary,
            onTertiary = CinemerickColors.darkOnTertiary,
            tertiaryContainer = CinemerickColors.darkTertiaryContainer,
            onTertiaryContainer = CinemerickColors.darkOnTertiaryContainer,
            error = CinemerickColors.darkError,
            onError = CinemerickColors.darkOnError,
            errorContainer = CinemerickColors.darkErrorContainer,
            onErrorContainer = CinemerickColors.darkOnErrorContainer,
            background = CinemerickColors.darkBackground,
            onBackground = CinemerickColors.darkOnBackground,
            surface = CinemerickColors.darkSurface,
            onSurface = CinemerickColors.darkOnSurface,
            surfaceVariant = CinemerickColors.darkSurfaceVariant,
            onSurfaceVariant = CinemerickColors.darkOnSurfaceVariant,
            surfaceContainerLowest = CinemerickColors.darkSurfaceContainerLowest,
            surfaceContainerLow = CinemerickColors.darkSurfaceContainerLow,
            surfaceContainer = CinemerickColors.darkSurfaceContainer,
            surfaceContainerHigh = CinemerickColors.darkSurfaceContainerHigh,
            surfaceContainerHighest = CinemerickColors.darkSurfaceContainerHighest,
            outline = CinemerickColors.darkOutline,
            outlineVariant = CinemerickColors.darkOutlineVariant,
            scrim = CinemerickColors.darkScrim,
        )
    } else {
        lightColorScheme(
            primary = CinemerickColors.lightPrimary,
            onPrimary = CinemerickColors.lightOnPrimary,
            primaryContainer = CinemerickColors.lightPrimaryContainer,
            onPrimaryContainer = CinemerickColors.lightOnPrimaryContainer,
            secondary = CinemerickColors.lightSecondary,
            onSecondary = CinemerickColors.lightOnSecondary,
            secondaryContainer = CinemerickColors.lightSecondaryContainer,
            onSecondaryContainer = CinemerickColors.lightOnSecondaryContainer,
            tertiary = CinemerickColors.lightTertiary,
            onTertiary = CinemerickColors.lightOnTertiary,
            tertiaryContainer = CinemerickColors.lightTertiaryContainer,
            onTertiaryContainer = CinemerickColors.lightOnTertiaryContainer,
            error = CinemerickColors.lightError,
            onError = CinemerickColors.lightOnError,
            errorContainer = CinemerickColors.lightErrorContainer,
            onErrorContainer = CinemerickColors.lightOnErrorContainer,
            background = CinemerickColors.lightBackground,
            onBackground = CinemerickColors.lightOnBackground,
            surface = CinemerickColors.lightSurface,
            onSurface = CinemerickColors.lightOnSurface,
            surfaceVariant = CinemerickColors.lightSurfaceVariant,
            onSurfaceVariant = CinemerickColors.lightOnSurfaceVariant,
            surfaceContainerLowest = CinemerickColors.lightSurfaceContainerLowest,
            surfaceContainerLow = CinemerickColors.lightSurfaceContainerLow,
            surfaceContainer = CinemerickColors.lightSurfaceContainer,
            surfaceContainerHigh = CinemerickColors.lightSurfaceContainerHigh,
            surfaceContainerHighest = CinemerickColors.lightSurfaceContainerHighest,
            outline = CinemerickColors.lightOutline,
            outlineVariant = CinemerickColors.lightOutlineVariant,
            scrim = CinemerickColors.lightScrim,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CinemerickTypography,
        shapes = CinemerickShapes,
        content = content
    )
}
