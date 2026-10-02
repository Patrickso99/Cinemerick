package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.preichert.cinemerick.core.designsystem.CinemaBrandColors
import com.preichert.cinemerick.core.designsystem.CinemerickAdaptiveLayout
import com.preichert.cinemerick.core.designsystem.CinemerickHeader
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.TimeTextField
import com.preichert.cinemerick.core.designsystem.shimmer
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.presentation.ObserveAsEvents
import com.preichert.cinemerick.core.presentation.currentPlatform
import com.preichert.cinemerick.core.presentation.util.DeviceConfiguration
import com.preichert.cinemerick.core.presentation.util.currentDeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.italianName
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.app_subtitle
import com.preichert.cinemerick.feature.showtimes.presentation.resources.app_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_done
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cancel
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinema_cinergia
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinema_cristallo
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinema_notorious
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinema_the_space
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinema_uci
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinemas_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.clear
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy
import com.preichert.cinemerick.feature.showtimes.presentation.resources.days_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_show_all
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_results
import com.preichert.cinemerick.feature.showtimes.presentation.resources.open_cinema_app
import com.preichert.cinemerick.feature.showtimes.presentation.resources.options_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.pick_days
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_today
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_tomorrow
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_weekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.range_from
import com.preichert.cinemerick.feature.showtimes.presentation.resources.range_to
import com.preichert.cinemerick.feature.showtimes.presentation.resources.theme_dark
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_error
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_placeholder
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.preichert.cinemerick.feature.showtimes.presentation.resources.add_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_venues_found
import com.preichert.cinemerick.feature.showtimes.presentation.resources.search_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.retry
import com.preichert.cinemerick.feature.showtimes.presentation.resources.done
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShowtimesRoot(
    viewModel: ShowtimesViewModel = koinViewModel(),
    themeViewModel: ThemeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val darkPreference by themeViewModel.darkTheme.collectAsStateWithLifecycle()
    val darkTheme = darkPreference ?: isSystemInDarkTheme()
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

    CinemerickTheme(darkTheme = darkTheme) {
        ShowtimesScreen(
            state = state,
            onAction = viewModel::onAction,
            snackbarHostState = snackbarHostState,
            darkTheme = darkTheme,
            onDarkThemeChange = themeViewModel::setDarkTheme
        )
    }
}

@Composable
fun ShowtimesScreen(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    darkTheme: Boolean = isSystemInDarkTheme(),
    onDarkThemeChange: (Boolean) -> Unit = {}
) {
    val configuration = currentDeviceConfiguration()
    // Larger screens show the calendar inline in the filters panel instead of a dialog.
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
                        title = stringResource(Res.string.app_title),
                        subtitle = stringResource(Res.string.app_subtitle),
                        version = "${BuildKonfig.VERSION_NAME} (${BuildKonfig.VERSION_CODE})",
                        trailing = {
                            ThemeSwitch(darkTheme = darkTheme, onDarkThemeChange = onDarkThemeChange)
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

/**
 * Filters section: cinemas, days, time ranges, film filter, and generate button.
 */
@Composable
private fun ColumnScope.FiltersSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    inlineCalendar: Boolean
) {
    // Cinema selection by chain
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = stringResource(Res.string.cinemas_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )
            Chain.entries.forEach { chain ->
                val chainVenues = state.selectedVenues.filter { it.chain == chain }
                val (container, onContainer) = chain.badgeColors()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = chain.displayName(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        chainVenues.forEach { venue ->
                            FilterChip(
                                selected = true,
                                onClick = { onAction(ShowtimesAction.OnVenueToggle(venue)) },
                                label = { Text(venue.name, style = MaterialTheme.typography.labelLarge) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = container.copy(alpha = 0.6f),
                                    selectedLabelColor = onContainer.copy(alpha = 0.9f)
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = { onAction(ShowtimesAction.OnVenuePickerOpen(chain)) }
                        ) {
                            Text(stringResource(Res.string.add_venue), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }

    // Format filter, only after a search: selected chips are shown, deselected ones are hidden.
    if (state.hasGenerated && state.availableTags.isNotEmpty()) {
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = stringResource(Res.string.formats_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
                Text(
                    text = stringResource(Res.string.formats_hint),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                ) {
                    state.availableTags.forEach { tag ->
                        FilterChip(
                            selected = tag !in state.hiddenTags,
                            onClick = { onAction(ShowtimesAction.OnFormatToggle(tag)) },
                            label = { Text(tag) }
                        )
                    }
                }
                if (state.hiddenTags.isNotEmpty()) {
                    TextButton(
                        onClick = { onAction(ShowtimesAction.OnFormatsReset) },
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    ) {
                        Text(stringResource(Res.string.formats_show_all))
                    }
                }
            }
        }
    }

    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = stringResource(Res.string.days_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.padding(horizontal = Spacing.lg)
            ) {
                AssistChip(
                    onClick = { onAction(ShowtimesAction.OnSelectToday) },
                    label = { Text(stringResource(Res.string.quick_today)) }
                )
                AssistChip(
                    onClick = { onAction(ShowtimesAction.OnSelectTomorrow) },
                    label = { Text(stringResource(Res.string.quick_tomorrow)) }
                )
                AssistChip(
                    onClick = { onAction(ShowtimesAction.OnSelectWeekend) },
                    label = { Text(stringResource(Res.string.quick_weekend)) }
                )
            }
            val calendar = state.calendar
            if (inlineCalendar && calendar != null) {
                MultiDateCalendar(
                    selected = state.days.mapTo(mutableSetOf()) { it.date },
                    minDate = calendar.minDate,
                    onToggle = { onAction(ShowtimesAction.OnDayToggle(it)) },
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )
                TextButton(
                    onClick = { onAction(ShowtimesAction.OnCalendarDismiss) },
                    modifier = Modifier.padding(horizontal = Spacing.md)
                ) {
                    Text(stringResource(Res.string.calendar_done))
                }
            } else {
                FilledTonalButton(
                    onClick = { onAction(ShowtimesAction.OnCalendarOpen) },
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                ) {
                    Text(stringResource(Res.string.pick_days))
                }
            }
            if (state.days.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(state.days, key = { it.date.toString() }) { day ->
                        DayChip(day = day, onClick = { onAction(ShowtimesAction.OnDayToggle(day.date)) })
                    }
                }
            }
        }
    }

    // Time ranges for selected days
    state.days.filter { it.isSelected }.forEach { day ->
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            DayRangeRow(day = day, onAction = onAction)
        }
    }

    // Film filter
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        val focusManager = LocalFocusManager.current
        OutlinedTextField(
            value = state.filmFilter,
            onValueChange = { onAction(ShowtimesAction.OnFilmFilterChange(it)) },
            label = { Text(stringResource(Res.string.film_filter)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                onAction(ShowtimesAction.OnGenerateClick)
            }),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        )
    }

    // Generate button
    Button(
        onClick = { onAction(ShowtimesAction.OnGenerateClick) },
        enabled = !state.isLoading,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = Spacing.sm).size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        }
        Text(stringResource(Res.string.generate), style = MaterialTheme.typography.titleMedium)
    }

    // Clear button
    OutlinedButton(
        onClick = { onAction(ShowtimesAction.OnClearClick) },
        enabled = state.canClear,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.clear))
    }
}

/**
 * Two-line day chip: weekday on top, day/month below.
 */
@Composable
private fun DayChip(day: DayUi, onClick: () -> Unit) {
    FilterChip(
        selected = day.isSelected,
        onClick = onClick,
        modifier = Modifier.height(56.dp),
        shape = MaterialTheme.shapes.medium,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = Spacing.xs)
            ) {
                Text(
                    text = day.date.dayOfWeek.italianName().take(3).uppercase(),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = day.date.shortDate(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    )
}

private fun LocalDate.shortDate(): String =
    "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"

/**
 * Results section: cinema errors, loading skeleton, generated options, and groups.
 */
@Composable
private fun ColumnScope.ResultsSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    configuration: DeviceConfiguration
) {
    // Errors
    state.venueErrors.forEach { error ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                modifier = Modifier.padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text("⚠", color = MaterialTheme.colorScheme.onErrorContainer)
                Text(
                    text = "${error.venueName}: ${error.message.asString()}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Loading skeleton
    if (state.isLoading) {
        repeat(3) { SkeletonCard() }
        return
    }

    // Hint before the first generation / empty state afterwards
    if (!state.hasGenerated) {
        MessageCard(emoji = "🎬", text = stringResource(Res.string.generate_hint))
    } else if (state.groups.isEmpty()) {
        MessageCard(emoji = "🍿", text = stringResource(Res.string.no_results))
    }

    // Copy button and options count
    if (state.pollText.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledTonalButton(
                onClick = { onAction(ShowtimesAction.OnCopyClick) },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(stringResource(Res.string.copy), style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text = buildString {
                    append(stringResource(Res.string.options_count, state.optionsCount))
                    state.elapsedTimeMillis?.let { millis ->
                        append(" (${millis / 1000}.${(millis % 1000).toString().padStart(3, '0')}s)")
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Results groups: single list on mobile, multi-column grid on larger screens
    if (configuration.isMobile) {
        state.groups.forEach { group -> AnimatedFilmGroupCard(group, large = false) }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = (maxWidth / MIN_GRID_CARD_WIDTH).toInt().coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                state.groups.chunked(columns).forEach { rowGroups ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        modifier = Modifier.height(IntrinsicSize.Max)
                    ) {
                        rowGroups.forEach { group ->
                            AnimatedFilmGroupCard(
                                group = group,
                                large = true,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                        // Keep the last row's cards the same width as the others
                        repeat(columns - rowGroups.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

private val MIN_GRID_CARD_WIDTH = 340.dp

@Composable
private fun AnimatedFilmGroupCard(
    group: FilmGroupUi,
    large: Boolean,
    modifier: Modifier = Modifier
) {
    val visibleState = remember(group) { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn() + slideInVertically { it / 8 },
        modifier = modifier
    ) {
        FilmGroupCard(group, large = large)
    }
}

@Composable
private fun FilmGroupCard(group: FilmGroupUi, large: Boolean) {
    SectionCard(modifier = Modifier.fillMaxSize()) {
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    group.posterUrl?.let {
                        PosterImage(
                            url = it,
                            contentDescription = group.title,
                            width = if (large) 132.dp else 96.dp
                        )
                    }
                    Text(
                        text = group.title,
                        color = MaterialTheme.colorScheme.primary,
                        style = if (large) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.xs))
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    group.showings.groupBy { it.date }.forEach { (date, showingsForDate) ->
                        val dayName = showingsForDate.firstOrNull()?.day ?: ""
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Badge(
                                text = "$dayName $date",
                                container = MaterialTheme.colorScheme.primary,
                                content = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            showingsForDate.forEach { showing -> ShowingRow(showing) }
                        }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterImage(url: String, contentDescription: String, width: Dp = 96.dp) {
    var isEnlarged by remember { mutableStateOf(false) }

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(width)
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { isEnlarged = true }
    )

    if (isEnlarged) {
        Dialog(onDismissRequest = { isEnlarged = false }) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { isEnlarged = false }
            )
        }
    }
}

// Tapping the badge opens the venue's app (or website); several options are offered in a dialog.
@Composable
private fun CinemaAppBadge(venue: Venue, container: Color, content: Color) {
    val uriHandler = LocalUriHandler.current
    val links = remember(venue) { venue.appLinks(currentPlatform) }
    var isChoosing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.clip(MaterialTheme.shapes.small).clickable {
            if (links.size == 1) uriHandler.openUri(links.first().url) else isChoosing = true
        }
    ) {
        Badge(text = venue.name, container = container, content = content)
    }

    if (isChoosing) {
        AlertDialog(
            onDismissRequest = { isChoosing = false },
            title = { Text(stringResource(Res.string.open_cinema_app, venue.name)) },
            text = {
                Column {
                    links.forEach { link ->
                        TextButton(onClick = {
                            isChoosing = false
                            uriHandler.openUri(link.url)
                        }) { Text(link.label) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isChoosing = false }) { Text(stringResource(Res.string.cancel)) }
            }
        )
    }
}

@Composable
private fun ShowingRow(showing: ShowingUi) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        itemVerticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Badge(
            text = showing.time,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            bold = true
        )
        val (container, content) = showing.venue.chain.badgeColors()
        CinemaAppBadge(venue = showing.venue, container = container, content = content)
        showing.format?.let {
            Badge(
                text = it,
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private fun Chain.displayName(): String = when (this) {
    Chain.THE_SPACE -> "The Space"
    Chain.UCI -> "UCI Cinemas"
    Chain.NOTORIOUS -> "Notorious"
    Chain.CINERGIA -> "Cinergia"
    Chain.CRISTALLO -> "Cristallo"
}

private fun Chain.badgeColors(): Pair<Color, Color> = when (this) {
    Chain.THE_SPACE -> CinemaBrandColors.theSpaceOrange to CinemaBrandColors.onTheSpaceOrange
    Chain.UCI -> CinemaBrandColors.uciBlue to CinemaBrandColors.onUciBlue
    Chain.NOTORIOUS -> CinemaBrandColors.notoriousSilver to CinemaBrandColors.onNotoriousSilver
    Chain.CINERGIA -> CinemaBrandColors.cinergia to CinemaBrandColors.onCinergia
    Chain.CRISTALLO -> CinemaBrandColors.cristallo to CinemaBrandColors.onCristallo
}

@Composable
private fun Badge(text: String, container: Color, content: Color, bold: Boolean = false) {
    Surface(shape = MaterialTheme.shapes.small, color = container) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (bold) FontWeight.Bold else null,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}

@Composable
private fun MessageCard(emoji: String, text: String) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SkeletonCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SkeletonLine(widthFraction = 0.5f, height = 18.dp)
            SkeletonLine(widthFraction = 0.9f, height = 14.dp)
            SkeletonLine(widthFraction = 0.7f, height = 14.dp)
        }
    }
}

@Composable
private fun SkeletonLine(widthFraction: Float, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .shimmer()
    )
}

@Composable
private fun DayRangeRow(
    day: DayUi,
    onAction: (ShowtimesAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text("🕘")
            Text(
                text = day.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        val errorText = stringResource(Res.string.time_error)
        val placeholder = stringResource(Res.string.time_placeholder)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TimeTextField(
                value = day.minTime,
                onValueChange = { onAction(ShowtimesAction.OnMinTimeChange(day.date, it)) },
                label = stringResource(Res.string.range_from),
                placeholder = placeholder,
                errorText = errorText.takeIf { day.isMinTimeError },
                modifier = Modifier.weight(1f)
            )
            TimeTextField(
                value = day.maxTime,
                onValueChange = { onAction(ShowtimesAction.OnMaxTimeChange(day.date, it)) },
                label = stringResource(Res.string.range_to),
                placeholder = placeholder,
                errorText = errorText.takeIf { day.isMaxTimeError },
                imeAction = ImeAction.Done,
                modifier = Modifier.weight(1f)
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

@Composable
private fun ThemeSwitch(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val description = stringResource(Res.string.theme_dark)
    Switch(
        checked = darkTheme,
        onCheckedChange = onDarkThemeChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color.White.copy(alpha = 0.45f),
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
            uncheckedBorderColor = Color.White.copy(alpha = 0.7f),
        ),
        modifier = Modifier.semantics { contentDescription = description }
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

@Preview
@Composable
private fun ShowtimesScreenDarkPreview() {
    CinemerickTheme(darkTheme = true) {
        ShowtimesScreen(
            state = previewState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            darkTheme = true
        )
    }
}

@Composable
private fun VenuePickerDialog(
    picker: VenuePickerUi,
    selectedVenues: Set<Venue>,
    onDismiss: () -> Unit,
    onSearchChange: (String) -> Unit,
    onVenueToggle: (Venue) -> Unit,
    onReload: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var localQuery by remember { mutableStateOf("") }
    var localSelected by remember { mutableStateOf(selectedVenues) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(Spacing.lg),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = stringResource(Res.string.search_venue),
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = localQuery,
                    onValueChange = { localQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.search_venue)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                when (val catalog = picker.catalog) {
                    is VenueCatalogUi.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is VenueCatalogUi.Loaded -> {
                        val filtered = remember(catalog.venues, localQuery) {
                            filteredVenues(catalog.venues, localQuery)
                        }
                        if (filtered.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.no_venues_found),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                items(filtered, key = { it.key }) { venue ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                localSelected = if (venue in localSelected) {
                                                    localSelected - venue
                                                } else {
                                                    localSelected + venue
                                                }
                                            }
                                            .padding(Spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                                    ) {
                                        Checkbox(
                                            checked = venue in localSelected,
                                            onCheckedChange = {
                                                localSelected = if (venue in localSelected) {
                                                    localSelected - venue
                                                } else {
                                                    localSelected + venue
                                                }
                                            }
                                        )
                                        Column {
                                            Text(venue.name)
                                            venue.region?.let {
                                                Text(
                                                    it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is VenueCatalogUi.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Text(
                                text = catalog.message.asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            OutlinedButton(onClick = onReload) {
                                Text(stringResource(Res.string.retry))
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(onClick = {
                        onSearchChange(localQuery)
                        val venuesToAdd = localSelected - selectedVenues
                        val venuesToRemove = selectedVenues - localSelected
                        venuesToAdd.forEach { onVenueToggle(it) }
                        venuesToRemove.forEach { onVenueToggle(it) }
                        onDismiss()
                    }) {
                        Text(stringResource(Res.string.done))
                    }
                }
            }
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
