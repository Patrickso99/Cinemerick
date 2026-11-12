package com.preichert.cinemerick.feature.settings.presentation

import com.preichert.cinemerick.core.designsystem.DarkCinemerickColors
import com.preichert.cinemerick.core.designsystem.LightCinemerickColors
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preichert.cinemerick.core.designsystem.CinemerickAdaptiveLayout
import com.preichert.cinemerick.core.designsystem.CinemerickCard
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickSectionLabel
import com.preichert.cinemerick.core.designsystem.CinemerickSegmentedControl
import com.preichert.cinemerick.core.designsystem.CinemerickSettingsRow
import com.preichert.cinemerick.core.designsystem.CinemerickSwitchRow
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.CinemerickTimeRangeSlider
import com.preichert.cinemerick.core.designsystem.TIME_WINDOW_MAX_MINUTES
import com.preichert.cinemerick.core.designsystem.CinemerickTimeText
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.presentation.toTimeString
import com.preichert.cinemerick.feature.settings.presentation.resources.Res
import com.preichert.cinemerick.feature.settings.presentation.resources.about_title
import com.preichert.cinemerick.feature.settings.presentation.resources.animations_smooth
import com.preichert.cinemerick.feature.settings.presentation.resources.animations_smooth_description
import com.preichert.cinemerick.feature.settings.presentation.resources.animations_title
import com.preichert.cinemerick.feature.settings.presentation.resources.app_theme_title
import com.preichert.cinemerick.feature.settings.presentation.resources.settings_back
import com.preichert.cinemerick.feature.settings.presentation.resources.settings_title
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_auto
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_auto_description
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_dark
import com.preichert.cinemerick.feature.settings.presentation.resources.theme_light
import com.preichert.cinemerick.feature.settings.presentation.resources.time_window_description
import com.preichert.cinemerick.feature.settings.presentation.resources.time_window_no_limit
import com.preichert.cinemerick.feature.settings.presentation.resources.time_window_title
import com.preichert.cinemerick.feature.settings.presentation.resources.tmdb_credit_description
import com.preichert.cinemerick.feature.settings.presentation.resources.tmdb_credit_open
import com.preichert.cinemerick.feature.settings.presentation.resources.language_english
import com.preichert.cinemerick.feature.settings.presentation.resources.language_italiano
import com.preichert.cinemerick.feature.settings.presentation.resources.language_title
import com.preichert.cinemerick.feature.settings.presentation.resources.tmdb_credit_title
import com.preichert.cinemerick.feature.settings.presentation.resources.tmdb_logo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.size
import com.preichert.cinemerick.core.domain.assistant.SavedKey
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_title
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_add_key
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_add_key_description
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_cloud_title
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_key_summary
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_key_verified
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_change_key
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_remove_key
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_privacy_cloud
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_privacy_none
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_language_hint
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_remove_title
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_remove_body
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_remove_keep
import com.preichert.cinemerick.feature.settings.presentation.resources.assistant_remove_confirm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoot(
    onNavigateBack: () -> Unit,
    onAssistantKeyClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()

    SettingsScreen(
        state = state,
        systemDark = systemDark,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onAssistantKeyClick = onAssistantKeyClick,
    )
}

/**
 * Settings, as in docs/design (Phone, 07): a back button and the title, then grouped sections: appearance
 * (Auto / Light / Dark), smooth animations, the default time window, the TMDB credit, and About with the version.
 */
@Composable
fun SettingsScreen(
    state: SettingsState,
    systemDark: Boolean,
    onAction: (SettingsAction) -> Unit,
    onNavigateBack: () -> Unit,
    onAssistantKeyClick: () -> Unit = {},
) {
    val backLabel = stringResource(Res.string.settings_back)
    val key = state.assistantKey
    if (state.isRemoveKeyDialogOpen && key != null) {
        RemoveKeyDialog(
            provider = key.provider.displayName,
            onConfirm = { onAction(SettingsAction.OnRemoveKeyConfirm) },
            onDismiss = { onAction(SettingsAction.OnRemoveKeyDismiss) },
        )
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0)
    ) { _ ->
        CinemerickAdaptiveLayout(
            header = {
                SettingsHeader(
                    title = stringResource(Res.string.settings_title),
                    backLabel = backLabel,
                    onBack = onNavigateBack,
                )
            },
            filtersContent = {
                SettingsGroup(label = stringResource(Res.string.app_theme_title)) {
                    ThemeModeControl(
                        selectedMode = state.themeMode,
                        onAutoSelect = { onAction(SettingsAction.OnAutoToggled(true, systemDark)) },
                        onModeSelect = { onAction(SettingsAction.OnThemeModeSelected(it)) },
                    )
                }
                SettingsGroup(label = stringResource(Res.string.animations_title)) {
                    CinemerickSwitchRow(
                        title = stringResource(Res.string.animations_smooth),
                        description = stringResource(Res.string.animations_smooth_description),
                        checked = state.animationsEnabled,
                        onCheckedChange = { onAction(SettingsAction.OnAnimationsToggled(it)) },
                    )
                }
                SettingsGroup(label = stringResource(Res.string.time_window_title)) {
                    TimeWindowSection(
                        minMinutes = state.defaultMinMinutes,
                        maxMinutes = state.defaultMaxMinutes,
                        onMinChange = { onAction(SettingsAction.OnDefaultMinTimeChanged(it)) },
                        onMaxChange = { onAction(SettingsAction.OnDefaultMaxTimeChanged(it)) },
                    )
                }
                SettingsGroup(label = stringResource(Res.string.language_title)) {
                    LanguageControl(
                        selected = state.language,
                        onSelect = { onAction(SettingsAction.OnLanguageSelected(it)) },
                    )
                }
                SettingsGroup(label = stringResource(Res.string.assistant_title)) {
                    AssistantSection(
                        key = state.assistantKey,
                        onKeyClick = onAssistantKeyClick,
                        onRemoveClick = { onAction(SettingsAction.OnRemoveKeyClick) },
                    )
                }
                SettingsGroup(label = stringResource(Res.string.tmdb_credit_title)) {
                    TmdbCreditSection()
                }
                SettingsGroup(label = stringResource(Res.string.about_title)) {
                    AboutRow()
                }
            }
        )
    }
}

@Composable
internal fun SettingsHeader(title: String, backLabel: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CinemerickIconButton(
            icon = CinemerickIcons.ArrowBack,
            contentDescription = backLabel,
            onClick = onBack,
            container = Color.Transparent,
        )
        Spacer(Modifier.width(Spacing.space2))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = CinemerickTheme.colors.onSurface,
        )
    }
}

/** Section label above a card holding the rows of one group. */
@Composable
private fun SettingsGroup(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        CinemerickSectionLabel(label)
        CinemerickCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = Spacing.space2)) {
                content()
            }
        }
    }
}

/**
 * Theme: a 48dp segmented control (Auto / Light / Dark) and three preview tiles. A tile selects the same mode as
 * the segment above it. The auto description explains what Auto follows.
 */
@Composable
private fun ThemeModeControl(
    selectedMode: ThemeMode,
    onAutoSelect: () -> Unit,
    onModeSelect: (ThemeMode) -> Unit,
) {
    val colors = CinemerickTheme.colors
    val selectedIndex = when (selectedMode) {
        ThemeMode.SYSTEM -> 0
        ThemeMode.LIGHT -> 1
        ThemeMode.DARK -> 2
    }
    val select: (Int) -> Unit = { index ->
        when (index) {
            0 -> onAutoSelect()
            1 -> onModeSelect(ThemeMode.LIGHT)
            else -> onModeSelect(ThemeMode.DARK)
        }
    }
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        CinemerickSegmentedControl(
            options = listOf(
                stringResource(Res.string.theme_auto) to CinemerickIcons.Auto,
                stringResource(Res.string.theme_light) to CinemerickIcons.Sun,
                stringResource(Res.string.theme_dark) to CinemerickIcons.Moon,
            ),
            selectedIndex = selectedIndex,
            onSelect = select,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            val autoLabel = stringResource(Res.string.theme_auto)
            val lightLabel = stringResource(Res.string.theme_light)
            val darkLabel = stringResource(Res.string.theme_dark)
            ThemeTile(
                label = autoLabel,
                selected = selectedIndex == 0,
                preview = ThemeTilePreview.AUTO,
                onClick = { select(0) },
                modifier = Modifier.weight(1f),
            )
            ThemeTile(
                label = lightLabel,
                selected = selectedIndex == 1,
                preview = ThemeTilePreview.LIGHT,
                onClick = { select(1) },
                modifier = Modifier.weight(1f),
            )
            ThemeTile(
                label = darkLabel,
                selected = selectedIndex == 2,
                preview = ThemeTilePreview.DARK,
                onClick = { select(2) },
                modifier = Modifier.weight(1f),
            )
        }
        if (selectedMode == ThemeMode.SYSTEM) {
            Text(
                text = stringResource(Res.string.theme_auto_description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

private enum class ThemeTilePreview { AUTO, LIGHT, DARK }

// The tiles draw each theme as it is, whatever the theme on screen, so they read the palettes of both themes.
private val LightThemePreviewBackground = LightCinemerickColors.background
private val DarkThemePreviewBackground = DarkCinemerickColors.background
private val LightThemePreviewAccent = LightCinemerickColors.primary
private val DarkThemePreviewAccent = DarkCinemerickColors.primary
private val LightThemePreviewBar = LightCinemerickColors.surfaceContainerHigh
private val DarkThemePreviewBar = DarkCinemerickColors.surfaceContainer
private val AutoThemePreviewBar = DarkCinemerickColors.onSurfaceVariant.copy(alpha = 0.45f)

/**
 * 64dp preview of a theme: a miniature card with an accent bar and a muted bar. Auto splits the tile on a diagonal,
 * light on the left and dark on the right. The selected one gets a 2dp primary ring.
 */
@Composable
private fun ThemeTile(
    label: String,
    selected: Boolean,
    preview: ThemeTilePreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(MaterialTheme.shapes.small)
            .drawWithCache {
                val width = size.width
                val height = size.height
                // The boundary runs from 64% of the top edge to 37% of the bottom edge. The gradient follows its normal,
                // so the hard stops at 0.5 sit on the boundary.
                val normal = Offset(height, width * (0.64f - 0.37f))
                val center = Offset(width / 2f, height / 2f)
                val split = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to LightThemePreviewBackground,
                        0.5f to LightThemePreviewBackground,
                        0.5f to DarkThemePreviewBackground,
                        1f to DarkThemePreviewBackground,
                    ),
                    start = center - normal,
                    end = center + normal,
                )
                val background = when (preview) {
                    ThemeTilePreview.AUTO -> split
                    ThemeTilePreview.LIGHT -> SolidColor(LightThemePreviewBackground)
                    ThemeTilePreview.DARK -> SolidColor(DarkThemePreviewBackground)
                }
                val accent = if (preview == ThemeTilePreview.LIGHT) LightThemePreviewAccent else DarkThemePreviewAccent
                val bar = when (preview) {
                    ThemeTilePreview.AUTO -> AutoThemePreviewBar
                    ThemeTilePreview.LIGHT -> LightThemePreviewBar
                    ThemeTilePreview.DARK -> DarkThemePreviewBar
                }
                val inset = 12.dp.toPx()
                val barHeight = 12.dp.toPx()
                val corner = CornerRadius(barHeight / 2f)
                onDrawBehind {
                    drawRect(background)
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(inset, 12.dp.toPx()),
                        size = Size(width * 0.39f, barHeight),
                        cornerRadius = corner,
                    )
                    drawRoundRect(
                        color = bar,
                        topLeft = Offset(inset, 34.dp.toPx()),
                        size = Size(width - 2 * inset, barHeight),
                        cornerRadius = corner,
                    )
                }
            }
            .then(
                if (selected) Modifier.border(2.dp, colors.primary, MaterialTheme.shapes.small)
                else Modifier.border(1.dp, colors.outlineVariant, MaterialTheme.shapes.small)
            )
            .clickable(role = Role.RadioButton, onClickLabel = label, onClick = onClick)
            .semantics { this.selected = selected },
    )
}

/** Language choice, as in docs/design (Phone, 07): a segmented control with English and Italiano. */
@Composable
private fun LanguageControl(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    val languages = listOf(AppLanguage.ENGLISH, AppLanguage.ITALIANO)
    val labels = listOf(stringResource(Res.string.language_english), stringResource(Res.string.language_italiano))
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        CinemerickSegmentedControl(
            options = labels.mapIndexed { index, label ->
                label to (if (languages[index] == selected) CinemerickIcons.Check else null)
            },
            selectedIndex = languages.indexOf(selected),
            onSelect = { onSelect(languages[it]) },
        )
    }
}

/**
 * Default time window, with the same slider as the home screen (00:00 to 24:00, 15-minute steps). The end at 24:00
 * is "no limit". Moving a bound keeps the window valid: the bound that moves past the other one is set first.
 */
@Composable
private fun TimeWindowSection(
    minMinutes: Int,
    maxMinutes: Int?,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int?) -> Unit,
) {
    val noLimit = stringResource(Res.string.time_window_no_limit)
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            text = stringResource(Res.string.time_window_description),
            style = MaterialTheme.typography.bodySmall,
            color = CinemerickTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CinemerickTimeText(text = minMinutes.toTimeString(), color = CinemerickTheme.colors.primary)
            CinemerickTimeText(text = maxMinutes?.toTimeString() ?: noLimit, color = CinemerickTheme.colors.primary)
        }
        CinemerickTimeRangeSlider(
            startMinutes = minMinutes,
            endMinutes = maxMinutes ?: TIME_WINDOW_MAX_MINUTES,
            onChange = { newMin, newEnd ->
                val newMax = newEnd.takeIf { it < TIME_WINDOW_MAX_MINUTES }
                if (maxMinutes != null && newMin > maxMinutes) {
                    onMaxChange(newMax)
                    onMinChange(newMin)
                } else {
                    onMinChange(newMin)
                    onMaxChange(newMax)
                }
            },
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/**
 * The voice assistant: the provider and its masked key with Change and Remove, or a row to add a key. The privacy note
 * says where requests go, so the badge in the voice sheet is never the first time the user reads it.
 */
@Composable
private fun AssistantSection(key: SavedKey?, onKeyClick: () -> Unit, onRemoveClick: () -> Unit) {
    val colors = CinemerickTheme.colors
    if (key == null) {
        CinemerickSettingsRow(
            title = stringResource(Res.string.assistant_add_key),
            description = stringResource(Res.string.assistant_add_key_description),
            onClick = onKeyClick,
            trailing = { Icon(CinemerickIcons.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant) },
        )
    } else {
        CinemerickSettingsRow(
            title = stringResource(Res.string.assistant_cloud_title, key.provider.displayName),
            description = stringResource(Res.string.assistant_key_summary, key.lastFour, key.model),
            trailing = {
                Text(
                    text = stringResource(Res.string.assistant_key_verified),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(colors.primaryContainer)
                        .padding(horizontal = Spacing.space2, vertical = Spacing.space1),
                )
            },
        )
        Row(
            modifier = Modifier.padding(horizontal = Spacing.space2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            TextButton(onClick = onKeyClick) {
                Text(stringResource(Res.string.assistant_change_key), color = colors.primary, style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = onRemoveClick) {
                Text(stringResource(Res.string.assistant_remove_key), color = colors.errorText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Icon(if (key != null) CinemerickIcons.Cloud else CinemerickIcons.Lock, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
            Text(
                text = if (key != null) stringResource(Res.string.assistant_privacy_cloud, key.provider.displayName) else stringResource(Res.string.assistant_privacy_none),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Text(stringResource(Res.string.assistant_language_hint), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

/** Removing the key needs a decision (T6): pasting it again is a chore, and the assistant stops working. */
@Composable
private fun RemoveKeyDialog(provider: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = CinemerickTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.assistant_remove_title, provider)) },
        text = { Text(stringResource(Res.string.assistant_remove_body, provider)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(Res.string.assistant_remove_confirm), color = colors.errorText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.assistant_remove_keep), color = colors.primary) }
        },
        containerColor = colors.surfaceContainerHigh,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
    )
}

private const val TMDB_URL = "https://www.themoviedb.org"

// The credit TMDB requires for its data: a localized description, its logo (smaller than the app's own header), the
// notice in TMDB's own wording, and a link to the site.
@Composable
private fun TmdbCreditSection() {
    val uriHandler = LocalUriHandler.current
    val openLabel = stringResource(Res.string.tmdb_credit_open)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = openLabel, role = Role.Button) { uriHandler.openUri(TMDB_URL) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3)
    ) {
        Text(
            text = stringResource(Res.string.tmdb_credit_description),
            style = MaterialTheme.typography.bodyMedium,
            color = CinemerickTheme.colors.onSurface
        )
        Image(
            painter = painterResource(Res.drawable.tmdb_logo),
            contentDescription = stringResource(Res.string.tmdb_logo),
            modifier = Modifier
                .width(TMDB_LOGO_WIDTH)
                .aspectRatio(1094f / 142f),
            contentScale = ContentScale.Fit
        )
    }
}

private val TMDB_LOGO_WIDTH = 96.dp

/** App name and description, with the version on the right. The version moved here from the home header. */
@Composable
private fun AboutRow() {
    CinemerickSettingsRow(
        title = BuildKonfig.APP_NAME,
        description = "v${BuildKonfig.VERSION_NAME} (${BuildKonfig.VERSION_CODE})",
    )
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    LightThemePreview {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.LIGHT),
            systemDark = false,
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    DarkThemePreview {
        SettingsScreen(
            state = SettingsState(themeMode = ThemeMode.DARK, animationsEnabled = true, defaultMinMinutes = 18 * 60, defaultMaxMinutes = 23 * 60),
            systemDark = true,
            onAction = {},
            onNavigateBack = {},
        )
    }
}
