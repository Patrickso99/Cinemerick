package it.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import it.cinemerick.core.designsystem.CinemerickTheme
import it.cinemerick.core.designsystem.TimeTextField
import it.cinemerick.core.presentation.ObserveAsEvents
import it.cinemerick.core.presentation.UiText
import it.cinemerick.feature.showtimes.presentation.resources.Res
import it.cinemerick.feature.showtimes.presentation.resources.app_title
import it.cinemerick.feature.showtimes.presentation.resources.copy
import it.cinemerick.feature.showtimes.presentation.resources.days_title
import it.cinemerick.feature.showtimes.presentation.resources.film_filter
import it.cinemerick.feature.showtimes.presentation.resources.generate
import it.cinemerick.feature.showtimes.presentation.resources.no_results
import it.cinemerick.feature.showtimes.presentation.resources.options_count
import it.cinemerick.feature.showtimes.presentation.resources.range_from
import it.cinemerick.feature.showtimes.presentation.resources.range_to
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
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "title") {
                    Text(
                        text = stringResource(Res.string.app_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                item(key = "days") {
                    Text(
                        text = stringResource(Res.string.days_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
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
                items(
                    items = state.days.filter { it.isSelected },
                    key = { "range-${it.date}" }
                ) { day ->
                    DayRangeRow(day = day, onAction = onAction)
                }
                item(key = "filter") {
                    OutlinedTextField(
                        value = state.filmFilter,
                        onValueChange = { onAction(ShowtimesAction.OnFilmFilterChange(it)) },
                        label = { Text(stringResource(Res.string.film_filter)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item(key = "generate") {
                    Button(
                        onClick = { onAction(ShowtimesAction.OnGenerateClick) },
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp).size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text(stringResource(Res.string.generate))
                    }
                }
                items(items = state.cinemaErrors, key = { "error-${it.cinemaName}" }) { error ->
                    Text(
                        text = "${error.cinemaName}: ${error.message.asString()}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (state.hasGenerated && !state.isLoading && state.groups.isEmpty()) {
                    item(key = "empty") { Text(stringResource(Res.string.no_results)) }
                }
                if (state.pollText.isNotEmpty()) {
                    item(key = "copy") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(onClick = { onAction(ShowtimesAction.OnCopyClick) }) {
                                Text(stringResource(Res.string.copy))
                            }
                            Text(stringResource(Res.string.options_count, state.optionsCount))
                        }
                    }
                }
                items(items = state.groups, key = { "group-${it.title}" }) { group ->
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(group.title, fontWeight = FontWeight.Bold)
                            group.lines.forEach { line -> Text(line) }
                        }
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
                    DayUi(LocalDate(2026, 10, 1), "gio 01/10", isSelected = true),
                    DayUi(LocalDate(2026, 10, 2), "ven 02/10", isSelected = true, maxTime = "23:00"),
                    DayUi(LocalDate(2026, 10, 3), "sab 03/10")
                ),
                hasGenerated = true,
                pollText = "Digger (Giovedì - 21:00 - Silea)",
                optionsCount = 2,
                groups = listOf(
                    FilmGroupUi(
                        title = "Digger",
                        lines = listOf(
                            "Digger (Giovedì - 21:00 - Silea)",
                            "Digger (Giovedì - 21:30 - Marcon)"
                        )
                    )
                )
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
