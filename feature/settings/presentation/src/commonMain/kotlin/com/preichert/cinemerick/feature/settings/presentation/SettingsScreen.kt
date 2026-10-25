package com.preichert.cinemerick.feature.settings.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()

    SettingsScreen(
        state = state,
        systemDark = systemDark,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
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
    onNavigateBack: () -> Unit
) {
    val backLabel = stringResource(Res.string.settings_back)
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
                        systemDark = systemDark,
                        onAction = onAction,
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
                        onAction = onAction,
                    )
                }
                SettingsGroup(label = stringResource(Res.string.language_title)) {
                    LanguageControl(
                        selected = state.language,
                        onSelect = { onAction(SettingsAction.OnLanguageSelected(it)) },
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
private fun SettingsHeader(title: String, backLabel: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CinemerickIconButton(
            icon = painterResource(CinemerickIcons.ArrowBack),
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
    systemDark: Boolean,
    onAction: (SettingsAction) -> Unit,
) {
    val colors = CinemerickTheme.colors
    val selectedIndex = when (selectedMode) {
        ThemeMode.SYSTEM -> 0
        ThemeMode.LIGHT -> 1
        ThemeMode.DARK -> 2
    }
    val select: (Int) -> Unit = { index ->
        when (index) {
            0 -> onAction(SettingsAction.OnAutoToggled(true, systemDark))
            1 -> onAction(SettingsAction.OnThemeModeSelected(ThemeMode.LIGHT))
            else -> onAction(SettingsAction.OnThemeModeSelected(ThemeMode.DARK))
        }
    }
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.space3),
    ) {
        CinemerickSegmentedControl(
            options = listOf(
                stringResource(Res.string.theme_auto) to painterResource(CinemerickIcons.Auto),
                stringResource(Res.string.theme_light) to painterResource(CinemerickIcons.Sun),
                stringResource(Res.string.theme_dark) to painterResource(CinemerickIcons.Moon),
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

private val LightThemePreviewBackground = Color(0xFFFAF8FF)
private val DarkThemePreviewBackground = Color(0xFF0D0B14)
private val LightThemePreviewAccent = Color(0xFF5B3FD6)
private val DarkThemePreviewAccent = Color(0xFFA78BFA)
private val LightThemePreviewBar = Color(0xFFE7E3F5)
private val DarkThemePreviewBar = Color(0xFF262140)
private val AutoThemePreviewBar = Color(0xFFA9A3C2).copy(alpha = 0.45f)

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
                label to (if (languages[index] == selected) painterResource(CinemerickIcons.Check) else null)
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
    onAction: (SettingsAction) -> Unit,
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
                    onAction(SettingsAction.OnDefaultMaxTimeChanged(newMax))
                    onAction(SettingsAction.OnDefaultMinTimeChanged(newMin))
                } else {
                    onAction(SettingsAction.OnDefaultMinTimeChanged(newMin))
                    onAction(SettingsAction.OnDefaultMaxTimeChanged(newMax))
                }
            },
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
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
            contentDescription = "TMDB",
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
