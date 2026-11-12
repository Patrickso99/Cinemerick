package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.runtime.getValue
import kotlin.time.Clock
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import com.preichert.cinemerick.core.designsystem.CinemerickLayoutState
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import com.preichert.cinemerick.core.designsystem.cinemerickJumpFocus
import com.preichert.cinemerick.core.designsystem.cinemerickJumpTarget
import com.preichert.cinemerick.core.designsystem.cinemerickScrollObstruction
import com.preichert.cinemerick.core.designsystem.rememberCinemerickLayoutState
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.preichert.cinemerick.core.designsystem.CinemerickFab
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.core.presentation.ObserveAsEvents
import com.preichert.cinemerick.core.designsystem.currentDeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.app_subtitle
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy_poll
import com.preichert.cinemerick.feature.showtimes.presentation.resources.assistant_fab
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_applied
import com.preichert.cinemerick.feature.showtimes.presentation.resources.voice_undo
import com.preichert.cinemerick.core.presentation.rememberMicPermissionController
import org.jetbrains.compose.resources.getString
import com.preichert.cinemerick.feature.showtimes.presentation.resources.open_settings
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.withFrameNanos
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShowtimesRoot(
    onNavigateToSettings: () -> Unit,
    onNavigateToAssistantSettings: () -> Unit,
    viewModel: ShowtimesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val micPermission = rememberMicPermissionController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // LocalClipboard has no common way to build a plain-text entry in Compose 1.12, so the deprecated manager stays.
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val layoutState = rememberCinemerickLayoutState()
    val animationsEnabled = LocalAnimationsEnabled.current
    // A jump leaves this gap between the summary bar and the component it lands on.
    val jumpGapPx = with(LocalDensity.current) { Spacing.space3.toPx() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ShowtimesEvent.CopyToClipboard -> clipboardManager.setText(AnnotatedString(event.text))
            is ShowtimesEvent.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asStringAsync())
            }
            ShowtimesEvent.ShowVoiceUndo -> scope.launch {
                // Once the search is over: the placeholder items are replaced by the results, which would move the list.
                launch {
                    viewModel.state.first { !it.isLoading && it.hasGenerated }
                    withFrameNanos { }
                    layoutState.showResults(animationsEnabled, jumpGapPx)
                }
                val result = snackbarHostState.showSnackbar(
                    message = getString(Res.string.voice_applied),
                    actionLabel = getString(Res.string.voice_undo),
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onAction(ShowtimesAction.OnVoiceUndo)
            }
            is ShowtimesEvent.ScrollToFilter -> scope.launch {
                val found = layoutState.jumpTo(event.target, animationsEnabled, jumpGapPx)
                // Layouts without chain cards (chips) still land on the Cinemas section.
                if (!found && event.target is FilterTarget.ChainCard) {
                    layoutState.jumpTo(FilterTarget.Cinemas, animationsEnabled, jumpGapPx)
                }
            }
        }
    }

    ShowtimesScreen(
        state = state,
        onAction = viewModel::onAction,
        onSettingsClick = onNavigateToSettings,
        // The microphone permission lives with the screen: the view model gets its status with the tap.
        onAssistantClick = { viewModel.onAction(ShowtimesAction.OnAssistantClick(micPermission.status())) },
        onAllowMic = { micPermission.request { viewModel.onAction(ShowtimesAction.OnVoiceMicResult(it)) } },
        onOpenMicSettings = micPermission::openSettings,
        onOpenAssistantSettings = {
            viewModel.onAction(ShowtimesAction.OnVoiceDismiss)
            onNavigateToAssistantSettings()
        },
        snackbarHostState = snackbarHostState,
        layoutState = layoutState,
    )
}

// Room for the floating buttons (16dp margin, 56dp button) plus a 12dp gap, less the snackbar's own 12dp.
private val SnackbarBesideFabEnd = 72.dp

@Composable
fun ShowtimesScreen(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    onSettingsClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    layoutState: CinemerickLayoutState = rememberCinemerickLayoutState(),
    onAssistantClick: () -> Unit = {},
    onAllowMic: () -> Unit = {},
    onOpenMicSettings: () -> Unit = {},
    onOpenAssistantSettings: () -> Unit = {},
) {
    val configuration = currentDeviceConfiguration()
    val summary = remember(
        state.selectedVenues, state.days, state.hiddenTags, state.availableTags,
        state.hasGenerated, state.filmFilter, state.searchedFilmFilter, state.isLoading,
    ) { state.toFilterSummary() }
    val primary = CinemerickTheme.colors.primary
    // The panel needs the room the keyboard takes, and a jump leaves the field being typed in.
    val focusManager = LocalFocusManager.current
    // After a jump only the target is focusable, with a label that says where the jump landed.
    val focusedTarget = layoutState.focusedTargetKey as? FilterTarget
    val focusLabel = focusedTarget?.spokenLabel(summary, state.filmFilter)
    val jumpTarget: (FilterTarget, Shape) -> Modifier = remember(layoutState, primary, focusedTarget, focusLabel) {
        { target, shape ->
            val ring = Modifier.cinemerickJumpTarget(layoutState, target, shape, primary)
            if (target == focusedTarget && focusLabel != null) ring.cinemerickJumpFocus(layoutState, focusLabel) else ring
        }
    }
    val today: LocalDate = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val onFilmVisible: (String) -> Unit = { onAction(ShowtimesAction.OnFilmCardVisible(it)) }
    val onFilmDetailsClick: (String) -> Unit = { onAction(ShowtimesAction.OnFilmDetailsOpen(it)) }
    val onPosterClick: (String, String) -> Unit = { title, url -> onAction(ShowtimesAction.OnPosterEnlarge(title, url)) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        // The assistant and Copy poll float at the bottom end on every layout, stacked with the assistant on top, except
        // under the open summary panel. In the Scaffold's slot, so a snackbar rises above them instead of covering them.
        floatingActionButton = {
            if (!state.isFilterSummaryExpanded) {
                Column(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    verticalArrangement = Arrangement.spacedBy(Spacing.space3),
                    horizontalAlignment = Alignment.End,
                ) {
                    CinemerickFab(
                        icon = CinemerickIcons.Assistant,
                        contentDescription = stringResource(Res.string.assistant_fab),
                        onClick = onAssistantClick,
                        tonal = true,
                    )
                    if (state.pollText.isNotEmpty()) {
                        CinemerickFab(
                            icon = CinemerickIcons.Copy,
                            contentDescription = stringResource(Res.string.copy_poll),
                            onClick = { onAction(ShowtimesAction.OnCopyClick) },
                        )
                    }
                }
            }
        },
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            state.calendar?.let { calendar ->
                DatePickerDialog(
                    calendar = calendar,
                    onDayToggle = { onAction(ShowtimesAction.OnCalendarDayToggle(it)) },
                    onMonthChange = { onAction(ShowtimesAction.OnCalendarMonthChange(it)) },
                    onClear = { onAction(ShowtimesAction.OnCalendarClear) },
                    onConfirm = { onAction(ShowtimesAction.OnDatesConfirm) },
                    onDismiss = { onAction(ShowtimesAction.OnCalendarDismiss) }
                )
            }
            state.venuePicker?.let { picker ->
                VenuePickerSheet(
                    picker = picker,
                    onDismiss = { onAction(ShowtimesAction.OnVenuePickerDismiss) },
                    onSearchChange = { onAction(ShowtimesAction.OnVenueSearchChange(it)) },
                    onVenueToggle = { onAction(ShowtimesAction.OnVenueDraftToggle(it)) },
                    onSelectAll = { onAction(ShowtimesAction.OnVenueDraftSelectAll) },
                    onDeselectAll = { onAction(ShowtimesAction.OnVenueDraftDeselectAll) },
                    onConfirm = { onAction(ShowtimesAction.OnVenuePickerConfirm) },
                    onReload = { onAction(ShowtimesAction.OnVenuesReload) }
                )
            }
            state.filmDetails?.let { film ->
                FilmDetailsOverlay(
                    film = film,
                    onDismiss = { onAction(ShowtimesAction.OnFilmDetailsDismiss) },
                    onPersonClick = { onAction(ShowtimesAction.OnPersonOpen(it)) },
                    onSynopsisExpand = { onAction(ShowtimesAction.OnSynopsisExpand) },
                    onImageOpen = { onAction(ShowtimesAction.OnFilmImageOpen(it)) },
                    onImageDismiss = { onAction(ShowtimesAction.OnFilmImageDismiss) },
                )
            }
            state.personDetails?.let { person ->
                PersonDetailsOverlay(
                    person = person,
                    onDismiss = { onAction(ShowtimesAction.OnPersonDismiss) },
                    onBiographyExpand = { onAction(ShowtimesAction.OnBiographyExpand) }
                )
            }
            state.enlargedPoster?.let { poster ->
                EnlargedPosterDialog(poster = poster, onDismiss = { onAction(ShowtimesAction.OnPosterDismiss) })
            }
            val settingsLabel = stringResource(Res.string.open_settings)
            CinemerickAdaptiveLayout(
                header = {
                    HomeHeader(
                        isWideScreen = configuration.isWideScreen,
                        onSettingsClick = onSettingsClick,
                        settingsLabel = settingsLabel,
                    )
                },
                filtersContent = {
                    FiltersContent(
                        state = state,
                        today = today,
                        isWideScreen = configuration.isWideScreen,
                        onAction = onAction,
                        jumpTarget = jumpTarget,
                    )
                },
                resultsContent = {
                    ResultsSection(
                        venueErrors = state.venueErrors,
                        isLoading = state.isLoading,
                        hasGenerated = state.hasGenerated,
                        groups = state.groups,
                        showSummary = state.pollText.isNotEmpty(),
                        optionsCount = state.optionsCount,
                        elapsedTimeMillis = state.elapsedTimeMillis,
                        isMobile = configuration.isMobile,
                        onFilmVisible = onFilmVisible,
                        onFilmDetailsClick = onFilmDetailsClick,
                        onPosterClick = onPosterClick
                    )
                },
                mobileResultsItems = {
                    resultsItems(
                        venueErrors = state.venueErrors,
                        isLoading = state.isLoading,
                        hasGenerated = state.hasGenerated,
                        groups = state.groups,
                        showSummary = state.pollText.isNotEmpty(),
                        optionsCount = state.optionsCount,
                        elapsedTimeMillis = state.elapsedTimeMillis,
                        onFilmVisible = onFilmVisible,
                        onFilmDetailsClick = onFilmDetailsClick,
                        onPosterClick = onPosterClick
                    )
                },
                desktopRail = {
                    DesktopNavigationRail(
                        settingsLabel = stringResource(Res.string.nav_settings),
                        onSettingsClick = onSettingsClick,
                    )
                },
                layoutState = layoutState,
                // Phones only: on tablet and desktop the filter pane is always in view.
                summaryBar = if (configuration.isMobile) {
                    {
                        FilterSummaryBar(
                            summary = summary,
                            expanded = state.isFilterSummaryExpanded,
                            onToggle = {
                                focusManager.clearFocus()
                                onAction(ShowtimesAction.OnFilterSummaryToggle)
                            },
                            onTargetClick = {
                                focusManager.clearFocus()
                                onAction(ShowtimesAction.OnFilterSummaryTargetClick(it))
                            },
                            onUpdateClick = { onAction(ShowtimesAction.OnFilterSummaryUpdateClick) },
                            onSettingsClick = onSettingsClick,
                            barModifier = Modifier.cinemerickScrollObstruction(layoutState),
                        )
                    }
                } else null,
                summaryBarExpanded = state.isFilterSummaryExpanded,
                onSummaryBarDismiss = { onAction(ShowtimesAction.OnFilterSummaryDismiss) },
            )
            state.voice?.let { voice ->
                VoiceSheet(
                    voice = voice,
                    engine = state.engineUi(),
                    systemAi = state.systemAi,
                    filters = summary,
                    onAction = onAction,
                    onAllowMic = onAllowMic,
                    onOpenMicSettings = onOpenMicSettings,
                    onOpenAssistantSettings = onOpenAssistantSettings,
                )
            }
            // The snackbar sits beside the lowest floating button, on its line, so it never covers them. Without the
            // buttons (summary panel open) it takes the whole width.
            val fabsShown = !state.isFilterSummaryExpanded
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    // The snackbar has 12dp of its own around it: these bring it to 16dp from the edge, centred on the
                    // 56dp button that sits 16dp above the bottom.
                    .padding(start = Spacing.space1, bottom = Spacing.space2, end = if (fabsShown) SnackbarBesideFabEnd else Spacing.space1),
            )
        }
    }
}

/**
 * Filter sections in the order of docs/design: cinemas, days, time window, formats, film filter. This is where the
 * screen turns what each section reports into an action: the sections themselves only know their own values.
 */
@Composable
private fun ColumnScope.FiltersContent(
    state: ShowtimesState,
    today: LocalDate,
    isWideScreen: Boolean,
    onAction: (ShowtimesAction) -> Unit,
    jumpTarget: (FilterTarget, Shape) -> Modifier,
) {
    CinemasSection(
        selectedVenues = state.selectedVenues,
        expandedChains = state.expandedChains,
        isWideScreen = isWideScreen,
        onChainExpandToggle = { onAction(ShowtimesAction.OnChainExpandToggle(it)) },
        onVenuePickerOpen = { onAction(ShowtimesAction.OnVenuePickerOpen(it)) },
        onVenueRemove = { onAction(ShowtimesAction.OnVenueToggle(it)) },
        jumpTarget = jumpTarget,
    )

    DaysSection(
        days = state.days,
        today = today,
        isWideScreen = isWideScreen,
        calendarMonth = state.inlineCalendarMonth ?: today.firstOfMonth(),
        onCalendarOpen = { onAction(ShowtimesAction.OnCalendarOpen) },
        onTodayClick = { onAction(ShowtimesAction.OnSelectToday) },
        onTomorrowClick = { onAction(ShowtimesAction.OnSelectTomorrow) },
        onWeekendClick = { onAction(ShowtimesAction.OnSelectWeekend) },
        onDayToggle = { onAction(ShowtimesAction.OnDayToggle(it)) },
        onMonthChange = { onAction(ShowtimesAction.OnCalendarMonthChange(it)) },
        jumpTarget = jumpTarget,
    )

    TimeWindowSection(
        days = state.days,
        onRangeChange = { date, minTime, maxTime ->
            onAction(ShowtimesAction.OnMinTimeChange(date, minTime))
            onAction(ShowtimesAction.OnMaxTimeChange(date, maxTime))
        },
        jumpTarget = jumpTarget,
    )

    if (state.hasGenerated && state.availableTags.isNotEmpty()) {
        FormatsSection(
            availableTags = state.availableTags,
            hiddenTags = state.hiddenTags,
            onFormatToggle = { onAction(ShowtimesAction.OnFormatToggle(it)) },
            onShowAllClick = { onAction(ShowtimesAction.OnFormatsReset) },
            jumpTarget = jumpTarget,
        )
    }

    FilmSearchField(
        value = state.filmFilter,
        onValueChange = { onAction(ShowtimesAction.OnFilmFilterChange(it)) },
        onSearch = { onAction(ShowtimesAction.OnGenerateClick) },
        jumpTarget = jumpTarget,
    )

    SearchActionBar(
        canClear = state.canClear,
        isLoading = state.isLoading,
        onClearClick = { onAction(ShowtimesAction.OnClearClick) },
        onSearchClick = { onAction(ShowtimesAction.OnGenerateClick) }
    )
}

/** Large header of the home screen: app name and tagline, with the settings button. Version lives in Settings. */
@Composable
private fun HomeHeader(isWideScreen: Boolean, onSettingsClick: () -> Unit, settingsLabel: String) {
    val colors = CinemerickTheme.colors
    if (isWideScreen) {
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
            icon = CinemerickIcons.Sliders,
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

@Preview(heightDp = 2200)
@Composable
private fun ShowtimesScreenLoadingPreview() {
    CinemerickTheme(isDarkTheme = true) {
        ShowtimesScreen(
            state = previewState().copy(isLoading = true),
            onAction = {},
            onSettingsClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(heightDp = 2200)
@Composable
private fun ShowtimesScreenEmptyPreview() {
    CinemerickTheme(isDarkTheme = true) {
        ShowtimesScreen(
            state = previewState().copy(groups = emptyList(), optionsCount = 0, pollText = "", hasGenerated = true),
            onAction = {},
            onSettingsClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(heightDp = 2200)
@Composable
private fun ShowtimesScreenVenueErrorPreview() {
    CinemerickTheme(isDarkTheme = true) {
        ShowtimesScreen(
            state = previewState().copy(
                venueErrors = listOf(VenueErrorUi(venueName = "UCI Luxe Marcon", message = UiText.DynamicString("Connection timed out"))),
            ),
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
                text = BuildKonfig.APP_NAME.take(1),
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
private fun RailItem(label: String, icon: Painter, selected: Boolean, onClick: () -> Unit) {
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
            painter = icon,
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
