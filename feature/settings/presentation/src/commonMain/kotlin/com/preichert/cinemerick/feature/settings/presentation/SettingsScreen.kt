package com.preichert.cinemerick.feature.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.preichert.cinemerick.core.designsystem.CinemerickAdaptiveLayout
import com.preichert.cinemerick.core.designsystem.CinemerickHeader
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.presentation.util.currentDeviceConfiguration
import com.preichert.cinemerick.feature.settings.presentation.resources.Res
import com.preichert.cinemerick.feature.settings.presentation.resources.app_theme_title
import com.preichert.cinemerick.feature.settings.presentation.resources.settings_subtitle
import com.preichert.cinemerick.feature.settings.presentation.resources.settings_title
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_auto
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_auto_description
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_dark
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_light
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_preview_dark
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_preview_light
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_preview_wide_dark
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_preview_wide_light
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenContent(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()

    SettingsScreen(
        state = state,
        systemDark = systemDark,
        onAction = viewModel::onAction,
        onNavigateBack = { navController.popBackStack() }
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    systemDark: Boolean,
    onAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0)
    ) { _ ->
        CinemerickAdaptiveLayout(
            header = {
                CinemerickHeader(
                    title = stringResource(Res.string.settings_title),
                    subtitle = stringResource(Res.string.settings_subtitle),
                    trailing = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.semantics {
                                contentDescription = "Back"
                            }
                        ) {
                            Text(
                                "‹",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                )
            },
            filtersContent = {
                ThemeSection(
                    selectedMode = state.themeMode,
                    systemDark = systemDark,
                    onAction = onAction
                )
            }
        )
    }
}

@Composable
private fun ThemeSection(
    selectedMode: ThemeMode,
    systemDark: Boolean,
    onAction: (SettingsAction) -> Unit
) {
    val isAutoEnabled = selectedMode == ThemeMode.SYSTEM
    val configuration = currentDeviceConfiguration()
    val isMobile = configuration.isMobile

    SectionCard(modifier = Modifier.fillMaxWidth(), {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = stringResource(Res.string.app_theme_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.theme_auto),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.theme_auto_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAutoEnabled,
                    onCheckedChange = { enabled ->
                        onAction(SettingsAction.OnAutoToggled(enabled, systemDark))
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            if (isMobile) {
                SmartphoneThemePreview(
                    selectedMode = selectedMode,
                    systemDark = systemDark,
                    isAutoEnabled = isAutoEnabled,
                    onAction = onAction
                )
            } else {
                DesktopThemePreview(
                    selectedMode = selectedMode,
                    systemDark = systemDark,
                    isAutoEnabled = isAutoEnabled,
                    onAction = onAction
                )
            }
        }
    })
}

@Composable
private fun SmartphoneThemePreview(
    selectedMode: ThemeMode,
    systemDark: Boolean,
    isAutoEnabled: Boolean,
    onAction: (SettingsAction) -> Unit
) {
    val lightLabel = stringResource(Res.string.theme_light)
    val darkLabel = stringResource(Res.string.theme_dark)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SmartphoneThemeCard(
                label = lightLabel,
                isDark = false,
                isSelected = if (isAutoEnabled) !systemDark else selectedMode == ThemeMode.LIGHT,
                isEnabled = !isAutoEnabled,
                onSelect = {
                    onAction(SettingsAction.OnThemeModeSelected(ThemeMode.LIGHT))
                },
                modifier = Modifier.weight(1f)
            )

            SmartphoneThemeCard(
                label = darkLabel,
                isDark = true,
                isSelected = if (isAutoEnabled) systemDark else selectedMode == ThemeMode.DARK,
                isEnabled = !isAutoEnabled,
                onSelect = {
                    onAction(SettingsAction.OnThemeModeSelected(ThemeMode.DARK))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SmartphoneThemeCard(
    label: String,
    isDark: Boolean,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium,
            border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) {
                    Color(0xFF1C1B1F)
                } else {
                    Color(0xFFFFFBFE)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Image(
                painter = painterResource(
                    if (isDark) {
                        Res.drawable.theme_preview_dark
                    } else {
                        Res.drawable.theme_preview_light
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1080f / 2400f)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = label
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 10.dp)
            )
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                enabled = isEnabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun DesktopThemePreview(
    selectedMode: ThemeMode,
    systemDark: Boolean,
    isAutoEnabled: Boolean,
    onAction: (SettingsAction) -> Unit
) {
    if (isAutoEnabled) {
        val systemTheme = if (systemDark) ThemeMode.DARK else ThemeMode.LIGHT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ThemePreviewCard(
                label = stringResource(Res.string.theme_light),
                isDark = false,
                isSelected = systemTheme == ThemeMode.LIGHT,
                enabled = false,
                onSelect = {},
                modifier = Modifier.weight(1f)
            )

            ThemePreviewCard(
                label = stringResource(Res.string.theme_dark),
                isDark = true,
                isSelected = systemTheme == ThemeMode.DARK,
                enabled = false,
                onSelect = {},
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ThemePreviewCard(
                label = stringResource(Res.string.theme_light),
                isDark = false,
                isSelected = selectedMode == ThemeMode.LIGHT,
                enabled = true,
                onSelect = {
                    onAction(SettingsAction.OnThemeModeSelected(ThemeMode.LIGHT))
                },
                modifier = Modifier.weight(1f)
            )

            ThemePreviewCard(
                label = stringResource(Res.string.theme_dark),
                isDark = true,
                isSelected = selectedMode == ThemeMode.DARK,
                enabled = true,
                onSelect = {
                    onAction(SettingsAction.OnThemeModeSelected(ThemeMode.DARK))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    label: String,
    isDark: Boolean,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    Column(
        modifier = modifier.alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mock-up card with theme colors
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium,
            border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) {
                    Color(0xFF1C1B1F)  // Dark background
                } else {
                    Color(0xFFFFFBFE)  // Light background
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Image(
                painter = painterResource(
                    if (isDark) {
                        Res.drawable.theme_preview_wide_dark
                    } else {
                        Res.drawable.theme_preview_wide_light
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1912f / 888f)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Fit
            )
        }

        // Radio button and label
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = label
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        content = content
    )
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    CinemerickTheme {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.SYSTEM),
            systemDark = false,
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    CinemerickTheme(isDarkTheme = true) {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.DARK),
            systemDark = true,
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun SettingsScreenDesktopPreview() {
    CinemerickTheme {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.LIGHT),
            systemDark = false,
            onAction = {},
            onNavigateBack = {}
        )
    }
}