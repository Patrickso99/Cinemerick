package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preichert.cinemerick.core.designsystem.CinemerickAdaptiveLayout
import com.preichert.cinemerick.core.designsystem.CinemerickHeader
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.pressScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.presentation.ObserveAsEvents
import com.preichert.cinemerick.core.presentation.util.currentDeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.app_subtitle
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_done
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
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
        onAction = { action ->
            when (action) {
                ShowtimesAction.OnSettingsClick -> onNavigateToSettings()
                else -> viewModel.onAction(action)
            }
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ShowtimesScreen(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val configuration = currentDeviceConfiguration()
    val inlineCalendar = !configuration.isMobile

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            state.calendar?.takeIf { !inlineCalendar }?.let { calendar ->
                AlertDialog(
                    onDismissRequest = { onAction(ShowtimesAction.OnCalendarDismiss) },
                    text = {
                        MultiDateCalendar(
                            selected = state.days.mapTo(mutableSetOf()) { it.date },
                            minDate = calendar.minDate,
                            onToggle = { onAction(ShowtimesAction.OnDayToggle(it)) }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { onAction(ShowtimesAction.OnCalendarDismiss) }) {
                            Text(stringResource(Res.string.calendar_done))
                        }
                    }
                )
            }
            state.venuePicker?.let { picker ->
                VenuePickerDialog(
                    picker = picker,
                    selectedVenues = state.selectedVenues,
                    onDismiss = { onAction(ShowtimesAction.OnVenuePickerDismiss) },
                    onSearchChange = { onAction(ShowtimesAction.OnVenueSearchChange(it)) },
                    onVenueToggle = { onAction(ShowtimesAction.OnVenueToggle(it)) },
                    onReload = { onAction(ShowtimesAction.OnVenuesReload) }
                )
            }
            CinemerickAdaptiveLayout(
                header = {
                    CinemerickHeader(
                        title = BuildKonfig.APP_NAME,
                        subtitle = stringResource(Res.string.app_subtitle),
                        version = "${BuildKonfig.VERSION_NAME} (${BuildKonfig.VERSION_CODE})",
                        trailing = {
                            val settingsSource = remember { MutableInteractionSource() }
                            IconButton(
                                onClick = { onAction(ShowtimesAction.OnSettingsClick) },
                                interactionSource = settingsSource,
                                modifier = Modifier.pressScale(settingsSource)
                            ) {
                                Text("⚙️")
                            }
                        }
                    )
                },
                filtersContent = {
                    FiltersSection(
                        state = state,
                        onAction = onAction,
                        inlineCalendar = inlineCalendar
                    )
                },
                resultsContent = {
                    ResultsSection(
                        state = state,
                        onAction = onAction,
                        configuration = configuration
                    )
                }
            )
        }
    }
}

private fun previewState(): ShowtimesState {
    val sileaVenue = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
    val marconVenue = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
    return ShowtimesState(
        days = listOf(
            DayUi(LocalDate(2026, 10, 1), "gio 01/10", isSelected = true),
            DayUi(LocalDate(2026, 10, 2), "ven 02/10", isSelected = true, maxTime = "24"),
            DayUi(LocalDate(2026, 10, 3), "sab 03/10", isSelected = true, minTime = "25"),
            DayUi(LocalDate(2026, 10, 4), "dom 04/10")
        ),
        selectedVenues = setOf(sileaVenue, marconVenue),
        hasGenerated = true,
        pollText = "Film Name (Giovedì - 21:00 - Silea)",
        optionsCount = 2,
        groups = listOf(
            FilmGroupUi(
                title = "Film Name",
                showings = listOf(
                    ShowingUi("Giovedì", "01/10", "21:00", sileaVenue, "2D · VO"),
                    ShowingUi("Giovedì", "01/10", "21:30", marconVenue, "2D · ENG · sub ITA")
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
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
