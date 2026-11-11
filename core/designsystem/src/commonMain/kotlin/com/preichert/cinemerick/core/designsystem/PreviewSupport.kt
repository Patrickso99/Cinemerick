package com.preichert.cinemerick.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Wraps a composable to show it in light theme.
 * Used as a container for preview content (wrap-content sizing).
 */
@Composable
fun LightThemePreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CinemerickTheme(isDarkTheme = false) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * Wraps a composable to show it in dark theme.
 * Used as a container for preview content (wrap-content sizing).
 */
@Composable
fun DarkThemePreview(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CinemerickTheme(isDarkTheme = true) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * Annotation to apply multiple device previews at once.
 * Covers phone, tablet, and desktop form factors in standard sizes.
 */
@Preview(name = "Phone", widthDp = 360, heightDp = 780)
@Preview(name = "Tablet", widthDp = 800, heightDp = 1000)
@Preview(name = "Desktop", widthDp = 1280, heightDp = 800)
annotation class DevicePreviews
