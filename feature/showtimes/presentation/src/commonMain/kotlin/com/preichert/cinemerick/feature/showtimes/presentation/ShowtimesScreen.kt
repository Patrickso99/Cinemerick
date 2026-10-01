package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.preichert.cinemerick.core.designsystem.CinemerickAdaptiveLayout
import com.preichert.cinemerick.core.designsystem.CinemerickHeader
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.TimeTextField
import com.preichert.cinemerick.core.presentation.ObserveAsEvents
import com.preichert.cinemerick.core.presentation.UiText
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.app_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy
import com.preichert.cinemerick.feature.showtimes.presentation.resources.days_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_results
import com.preichert.cinemerick.feature.showtimes.presentation.resources.options_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.range_from
import com.preichert.cinemerick.feature.showtimes.presentation.resources.range_to
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShowtimesRoot(
    viewModel: ShowtimesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
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
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowtimesScreen(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            CinemerickAdaptiveLayout(
                header = {
                    CinemerickHeader(
                        title = stringResource(Res.string.app_title),
                        subtitle = "Cinema showtimes generator"
                    )
                },
                filtersContent = {
                    FiltersSection(state = state, onAction = onAction)
                },
                resultsContent = {
                    ResultsSection(state = state, onAction = onAction)
                }
            )
        }
    }
}

/**
 * Filters section: days, time ranges, film filter, and generate button.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.FiltersSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.days_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.days.forEach { day ->
                    FilterChip(
                        selected = day.isSelected,
                        onClick = { onAction(ShowtimesAction.OnDayToggle(day.date)) },
                        label = { Text(day.label) }
                    )
                }
            }
        }
    }

    // Time ranges for selected days
    state.days.filter { it.isSelected }.forEach { day ->
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayRangeRow(day = day, onAction = onAction)
            }
        }
    }

    // Film filter
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            OutlinedTextField(
                value = state.filmFilter,
                onValueChange = { onAction(ShowtimesAction.OnFilmFilterChange(it)) },
                label = { Text(stringResource(Res.string.film_filter)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Generate button
    Button(
        onClick = { onAction(ShowtimesAction.OnGenerateClick) },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = 8.dp).size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(stringResource(Res.string.generate))
    }
}

/**
 * Results section: cinema errors, generated options, and groups.
 */
@Composable
private fun ColumnScope.ResultsSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit
) {
    // Errors
    state.cinemaErrors.forEach { error ->
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "${error.cinemaName}: ${error.message.asString()}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }

    // Empty state
    if (state.hasGenerated && !state.isLoading && state.groups.isEmpty()) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.no_results),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Copy button and options count
    if (state.pollText.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledTonalButton(
                onClick = { onAction(ShowtimesAction.OnCopyClick) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(Res.string.copy))
            }
            Text(
                text = stringResource(Res.string.options_count, state.optionsCount),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // Results groups
    state.groups.forEach { group ->
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = group.title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall
                    )
                    group.lines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRangeRow(
    day: DayUi,
    onAction: (ShowtimesAction) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(day.label, modifier = Modifier.widthIn(min = 72.dp), fontWeight = FontWeight.Medium)
        TimeTextField(
            value = day.minTime,
            onValueChange = { onAction(ShowtimesAction.OnMinTimeChange(day.date, it)) },
            label = stringResource(Res.string.range_from),
            modifier = Modifier.weight(1f)
        )
        TimeTextField(
            value = day.maxTime,
            onValueChange = { onAction(ShowtimesAction.OnMaxTimeChange(day.date, it)) },
            label = stringResource(Res.string.range_to),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview
@Composable
private fun ShowtimesScreenPreview() {
    CinemerickTheme {
        ShowtimesScreen(
            state = ShowtimesState(
                days = listOf(
                    DayUi(LocalDate(2026, 10, 1), "Oct 1", isSelected = true),
                    DayUi(LocalDate(2026, 10, 2), "Oct 2", isSelected = true, maxTime = "23:00"),
                    DayUi(LocalDate(2026, 10, 3), "Oct 3")
                ),
                hasGenerated = true,
                pollText = "Film Name (Thursday - 21:00 - Cinema)",
                optionsCount = 2,
                groups = listOf(
                    FilmGroupUi(
                        title = "Film Name",
                        lines = listOf(
                            "Film Name (Thursday - 21:00 - Cinema 1)",
                            "Film Name (Thursday - 21:30 - Cinema 2)"
                        )
                    )
                )
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun ShowtimesScreenDarkPreview() {
    CinemerickTheme(darkTheme = true) {
        ShowtimesScreen(
            state = ShowtimesState(
                days = listOf(
                    DayUi(LocalDate(2026, 10, 1), "Oct 1", isSelected = true),
                    DayUi(LocalDate(2026, 10, 2), "Oct 2", isSelected = true, maxTime = "23:00"),
                ),
                hasGenerated = true,
                pollText = "Film Name (Thursday - 21:00 - Cinema)",
                optionsCount = 2,
                groups = listOf(
                    FilmGroupUi(
                        title = "Film Name",
                        lines = listOf(
                            "Film Name (Thursday - 21:00 - Cinema 1)",
                        )
                    )
                )
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
