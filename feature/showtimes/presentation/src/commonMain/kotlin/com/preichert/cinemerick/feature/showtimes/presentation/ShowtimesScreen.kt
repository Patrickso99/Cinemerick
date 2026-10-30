package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import org.jetbrains.compose.resources.DrawableResource
import com.preichert.cinemerick.feature.showtimes.presentation.resources.nav_settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preichert.cinemerick.core.designsystem.CinemerickAdaptiveLayout
import com.preichert.cinemerick.core.designsystem.CinemerickExtendedFab
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.presentation.ObserveAsEvents
import com.preichert.cinemerick.core.presentation.util.currentDeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.app_subtitle
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy_poll
import com.preichert.cinemerick.feature.showtimes.presentation.resources.open_settings
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShowtimesScreenContent(
    onNavigateToSettings: () -> Unit,
    viewModel: ShowtimesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ShowtimesEvent.CopyToClipboard -> clipboardManager.setText(AnnotatedString(event.text))
            is ShowtimesEvent.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
        }
    }

    ShowtimesScreen(
        state = state,
        onAction = viewModel::onAction,
        onSettingsClick = onNavigateToSettings,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ShowtimesScreen(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    onSettingsClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val configuration = currentDeviceConfiguration()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            state.calendar?.let { calendar ->
                DatePickerDialog(
                    initialDates = state.days.mapTo(mutableSetOf()) { it.date },
                    minDate = calendar.minDate,
                    onConfirm = { onAction(ShowtimesAction.OnDatesConfirm(it)) },
                    onDismiss = { onAction(ShowtimesAction.OnCalendarDismiss) }
                )
            }
            state.venuePicker?.let { picker ->
                VenuePickerSheet(
                    picker = picker,
                    selectedVenues = state.selectedVenues,
                    onDismiss = { onAction(ShowtimesAction.OnVenuePickerDismiss) },
                    onSearchChange = { onAction(ShowtimesAction.OnVenueSearchChange(it)) },
                    onVenueToggle = { onAction(ShowtimesAction.OnVenueToggle(it)) },
                    onReload = { onAction(ShowtimesAction.OnVenuesReload) }
                )
            }
            state.filmDetails?.let { film ->
                FilmDetailsOverlay(
                    film = film,
                    onDismiss = { onAction(ShowtimesAction.OnFilmDetailsDismiss) },
                    onPersonClick = { onAction(ShowtimesAction.OnPersonOpen(it)) },
                )
            }
            state.personDetails?.let { person ->
                PersonDetailsOverlay(
                    person = person,
                    onDismiss = { onAction(ShowtimesAction.OnPersonDismiss) }
                )
            }
            val settingsLabel = stringResource(Res.string.open_settings)
            CinemerickAdaptiveLayout(
                header = {
                    HomeHeader(
                        onSettingsClick = onSettingsClick,
                        settingsLabel = settingsLabel,
                    )
                },
                collapsedTitle = BuildKonfig.APP_NAME,
                collapsedAction = {
                    CinemerickIconButton(
                        icon = painterResource(CinemerickIcons.Sliders),
                        contentDescription = settingsLabel,
                        onClick = onSettingsClick,
                    )
                },
                filtersContent = {
                    FiltersSection(
                        state = state,
                        onAction = onAction
                    )
                },
                resultsContent = {
                    ResultsSection(
                        state = state,
                        onAction = onAction,
                        configuration = configuration
                    )
                },
                mobileResultsItems = { resultsItems(state, onAction) },
                desktopRail = {
                    DesktopNavigationRail(
                        settingsLabel = stringResource(Res.string.nav_settings),
                        onSettingsClick = onSettingsClick,
                    )
                },
            )
            // Copy poll floats at the bottom end on every layout.
            if (state.pollText.isNotEmpty()) {
                CinemerickExtendedFab(
                    text = stringResource(Res.string.copy_poll),
                    icon = painterResource(CinemerickIcons.Copy),
                    onClick = { onAction(ShowtimesAction.OnCopyClick) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(end = Spacing.space4, bottom = Spacing.space4),
                )
            }
        }
    }
}

/** Large header of the home screen: app name and tagline, with the settings button. Version lives in Settings. */
@Composable
private fun HomeHeader(onSettingsClick: () -> Unit, settingsLabel: String) {
    val colors = CinemerickTheme.colors
    if (currentDeviceConfiguration().isWideScreen) {
        // Two-pane layouts: the title alone, as in docs/design (Large). The settings button is in the rail.
        Text(
            text = BuildKonfig.APP_NAME,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = Spacing.space2),
        )
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = BuildKonfig.APP_NAME,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(Res.string.app_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
        }
        CinemerickIconButton(
            icon = painterResource(CinemerickIcons.Sliders),
            contentDescription = settingsLabel,
            onClick = onSettingsClick,
        )
    }
}

@Composable
private fun previewState(): ShowtimesState {
    val sileaVenue = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
    val marconVenue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
    return ShowtimesState(
        days = listOf(
            DayUi(LocalDate(2026, 10, 1), isSelected = true, minTime = "20:00", maxTime = ""),
            DayUi(LocalDate(2026, 10, 2), isSelected = true, maxTime = "24", minTime = "20:00"),
            DayUi(LocalDate(2026, 10, 3), isSelected = true, minTime = "25", maxTime = ""),
            DayUi(LocalDate(2026, 10, 4), minTime = "20:00", maxTime = "")
        ),
        selectedVenues = setOf(sileaVenue, marconVenue),
        hasGenerated = true,
        pollText = "Film Name (Giovedì - 21:00 - Silea)",
        optionsCount = 2,
        groups = listOf(
            FilmGroupUi(
                title = "Film Name",
                showings = listOf(
                    ShowingUi(DayOfWeek.THURSDAY, "01/10", "21:00", sileaVenue, "2D · VO"),
                    ShowingUi(DayOfWeek.THURSDAY, "01/10", "21:30", marconVenue, "2D · ENG · sub ITA")
                )
            )
        )
    )
}

@Preview
@Composable
private fun ShowtimesScreenPreview() {
    CinemerickTheme {
        ShowtimesScreen(
            state = previewState(),
            onAction = {},
            onSettingsClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun ShowtimesScreenDarkPreview() {
    CinemerickTheme(isDarkTheme = true) {
        ShowtimesScreen(
            state = previewState(),
            onAction = {},
            onSettingsClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun ShowtimesScreenDesktopPreview() {
    CinemerickTheme {
        ShowtimesScreen(
            state = previewState().let { it.copy(groups = it.groups + it.groups + it.groups) },
            onAction = {},
            onSettingsClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

/**
 * Desktop navigation rail, 84dp: the app mark, Search (the current screen) and Settings at the bottom.
 * Each item is 64x60dp with a label under its icon.
 */
@Composable
private fun DesktopNavigationRail(
    settingsLabel: String,
    onSettingsClick: () -> Unit,
) {
    val colors = CinemerickTheme.colors
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(84.dp)
            .background(colors.surfaceContainer)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "C",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onPrimary,
            )
        }
        Spacer(Modifier.height(Spacing.space3))
        Spacer(Modifier.weight(1f))
        RailItem(label = settingsLabel, icon = CinemerickIcons.Sliders, selected = false, onClick = onSettingsClick)
    }
}

@Composable
private fun RailItem(label: String, icon: DrawableResource, selected: Boolean, onClick: () -> Unit) {
    val colors = CinemerickTheme.colors
    Column(
        modifier = Modifier
            .size(width = 64.dp, height = 60.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(if (selected) colors.primaryContainer else Color.Transparent)
            .clickable(role = Role.Tab, onClickLabel = label, onClick = onClick)
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
