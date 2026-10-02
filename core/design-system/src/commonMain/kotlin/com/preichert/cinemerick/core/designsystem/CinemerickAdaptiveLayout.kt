package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.presentation.util.DeviceConfiguration
import com.preichert.cinemerick.core.presentation.util.currentDeviceConfiguration

/**
 * Adaptive layout for Cinemerick app, responding to screen size and orientation.
 *
 * @param header A header composable (typically with gradient background)
 * @param filtersContent The filters section (days, time range, film search)
 * @param resultsContent The results section (cinema showtimes)
 * @param modifier Layout modifier
 */
@Composable
fun CinemerickAdaptiveLayout(
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    filtersContent: @Composable ColumnScope.() -> Unit,
    resultsContent: @Composable ColumnScope.() -> Unit,
) {
    val configuration = currentDeviceConfiguration()

    when (configuration) {
        // Single column on mobile portrait: header, filters, results stacked
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                header()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    filtersContent()
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    resultsContent()
                }
                Spacer(modifier = Modifier.height(Spacing.xl))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }

        // Two-column layout on mobile landscape: logo + filters on left, results on right
        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                ) {
                    header()
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        filtersContent()
                    }
                }
                Panel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    scrollable = true,
                ) {
                    resultsContent()
                }
            }
        }

        // Tablet portrait: centered content, side-by-side filters | results
        DeviceConfiguration.TABLET_PORTRAIT -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                header()
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    Panel(modifier = Modifier.weight(1f)) {
                        filtersContent()
                    }
                    Panel(modifier = Modifier.weight(1f)) {
                        resultsContent()
                    }
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }

        // Tablet landscape and desktop: fixed header, two panels scrolling independently
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            val isDesktop = configuration == DeviceConfiguration.DESKTOP
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                header()
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                        .widthIn(max = if (isDesktop) 1400.dp else 1200.dp)
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    Panel(
                        modifier = Modifier
                            .weight(if (isDesktop) 0.8f else 1f)
                            .fillMaxHeight(),
                        scrollable = true,
                    ) {
                        filtersContent()
                    }
                    Panel(
                        modifier = Modifier
                            .weight(if (isDesktop) 1.2f else 1f)
                            .fillMaxHeight(),
                        scrollable = true,
                    ) {
                        resultsContent()
                    }
                }
            }
        }
    }
}

/**
 * Panel that groups a section on wide layouts: tonal surface with a subtle outline.
 */
@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            content = content,
        )
    }
}

/**
 * Gradient header with decorative circles, used for app branding.
 */
@Composable
fun CinemerickHeader(
    title: String,
    subtitle: String? = null,
    version: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val brush = Brush.linearGradient(if (isDark) PurpleGradientDark else PurpleGradient)
    val accent = MaterialTheme.colorScheme.secondaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(brush)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.08f), radius = size.height * 0.9f, center = Offset(size.width * 0.95f, size.height * 0.1f))
                drawCircle(accent.copy(alpha = 0.18f), radius = size.height * 0.45f, center = Offset(size.width * 0.78f, size.height * 0.95f))
            }
            // Gradient extends behind the status bar; content stays clear of it.
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
            .padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.xl, bottom = Spacing.xxl),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                    )
                    if (version != null) {
                        Text(
                            text = "v$version",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = Spacing.xs),
                        )
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
            trailing?.invoke()
        }
    }
}
