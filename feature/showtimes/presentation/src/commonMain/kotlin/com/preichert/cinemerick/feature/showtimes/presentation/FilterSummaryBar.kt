package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickSummaryBar
import com.preichert.cinemerick.core.designsystem.CinemerickSummaryDot
import com.preichert.cinemerick.core.designsystem.CinemerickSummaryDots
import com.preichert.cinemerick.core.designsystem.CinemerickSummaryGroupLabel
import com.preichert.cinemerick.core.designsystem.CinemerickSummaryRow
import com.preichert.cinemerick.core.designsystem.CinemerickSummaryRowIcon
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.SummaryDotsMax
import com.preichert.cinemerick.core.presentation.toTimeString
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinemas_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_window_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.days_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.chain_no_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.open_settings
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_all_films
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_changed
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_day
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_days
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_description
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_film_quoted
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_flag_changed
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_formats_hidden
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_go_to
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_group_days_times
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_group_format_film
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_hide
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_no_day
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_no_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_panel_changed_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_panel_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_panel_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_pick_day
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_pick_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_searching
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_show
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_times_per_day
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_update
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_update_results
import com.preichert.cinemerick.feature.showtimes.presentation.resources.summary_venues
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_range
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Days listed by name in the bar title; more than this become a count ("5 days"). */
private const val NamedDaysMax = 2

/**
 * The filter summary bar of the phone layouts: the filters in two lines, opening into one row per filter. A row
 * jumps to its component ([onTargetClick]). When the film filter changed after the search, Update replaces Settings.
 *
 * @param barModifier marks the bar row, e.g. as the area a jump lands below
 */
@Composable
internal fun FilterSummaryBar(
    summary: FilterSummaryUi,
    expanded: Boolean,
    onToggle: () -> Unit,
    onTargetClick: (FilterTarget) -> Unit,
    onUpdateClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    barModifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    val title = summaryTitle(summary, colors.primary)
    val subtitle = summarySubtitle(summary)
    val showUpdate = summary.isFilmFilterChanged && !expanded
    CinemerickSummaryBar(
        title = if (expanded) AnnotatedString(stringResource(Res.string.summary_panel_title)) else title,
        subtitle = when {
            expanded && summary.isStale -> stringResource(Res.string.summary_panel_changed_hint)
            expanded -> stringResource(Res.string.summary_panel_hint)
            else -> subtitle
        },
        expanded = expanded,
        onToggle = onToggle,
        description = if (expanded) stringResource(Res.string.summary_panel_title) else stringResource(Res.string.summary_description, title.text, subtitle),
        toggleLabel = stringResource(if (expanded) Res.string.summary_hide else Res.string.summary_show),
        modifier = modifier,
        barModifier = barModifier,
        emphasizedSubtitle = summary.isStale && !expanded,
        busy = summary.isSearching && !expanded,
        leading = if (expanded || summary.chains.isEmpty()) null else ({
            CinemerickSummaryDots(
                colors = summary.chains.take(SummaryDotsMax).map { it.chain.badgeColors().first },
                more = (summary.chains.size - SummaryDotsMax).coerceAtLeast(0),
            )
        }),
        trailing = {
            if (showUpdate) {
                CinemerickPrimaryButton(
                    text = stringResource(Res.string.summary_update),
                    onClick = onUpdateClick,
                    enabled = !summary.isSearching,
                    loading = summary.isSearching,
                    height = 40.dp,
                )
            } else {
                CinemerickIconButton(
                    icon = CinemerickIcons.Sliders,
                    contentDescription = stringResource(Res.string.open_settings),
                    onClick = onSettingsClick,
                    container = Color.Transparent,
                    tint = colors.onSurfaceVariant,
                )
            }
        },
    ) {
        SummaryPanel(summary = summary, onTargetClick = onTargetClick, onUpdateClick = onUpdateClick)
    }
}

/** "2 venues · Thu 13, Fri 14"; a missing venue or day is the next thing to do, so it is written in [accent]. */
@Composable
private fun summaryTitle(summary: FilterSummaryUi, accent: Color): AnnotatedString {
    val venues = if (summary.venueCount == 0) null else pluralStringResource(Res.plurals.summary_venues, summary.venueCount, summary.venueCount)
    val noVenue = stringResource(Res.string.summary_no_venue)
    val days = when {
        summary.days.isEmpty() -> null
        summary.days.size <= NamedDaysMax -> summary.days.map { it.date.summaryDay() }.joinToString(", ")
        else -> pluralStringResource(Res.plurals.summary_days, summary.days.size, summary.days.size)
    }
    val noDay = stringResource(Res.string.summary_no_day)
    return buildAnnotatedString {
        if (venues != null) append(venues) else withStyle(SpanStyle(color = accent)) { append(noVenue) }
        append(" · ")
        if (days != null) append(days) else withStyle(SpanStyle(color = accent)) { append(noDay) }
    }
}

/** "20:00–24:00 · 3D hidden · “Dune, Wicked”", or the search state when there is one. */
@Composable
private fun summarySubtitle(summary: FilterSummaryUi): String {
    if (summary.isSearching) return stringResource(Res.string.summary_searching)
    if (summary.isStale) return stringResource(Res.string.summary_changed)
    val window = summary.sharedWindow
    val parts = buildList {
        when {
            summary.days.isEmpty() -> Unit
            window != null -> add(stringResource(Res.string.time_range, window.first.toTimeString(), window.second.toTimeString()))
            else -> add(stringResource(Res.string.summary_times_per_day))
        }
        if (summary.hiddenFormats.isNotEmpty()) {
            add(pluralStringResource(Res.plurals.summary_formats_hidden, summary.hiddenFormats.size, summary.hiddenFormats.joinToString(", ")))
        }
        add(
            if (summary.filmQueries.isEmpty()) stringResource(Res.string.summary_all_films)
            else stringResource(Res.string.summary_film_quoted, summary.filmQueries.joinToString(", "))
        )
    }
    return parts.joinToString(" · ")
}

/** Groups of rows: cinemas (chain, venues below), days and times, format and film; Update when the results are stale. */
@Composable
private fun SummaryPanel(summary: FilterSummaryUi, onTargetClick: (FilterTarget) -> Unit, onUpdateClick: () -> Unit) {
    val goTo = stringResource(Res.string.summary_go_to)
    CinemerickSummaryGroupLabel(stringResource(Res.string.cinemas_title))
    if (summary.chains.isEmpty()) {
        CinemerickSummaryRow(
            title = AnnotatedString(stringResource(Res.string.summary_pick_venue)),
            onClick = { onTargetClick(FilterTarget.Cinemas) },
            onClickLabel = goTo,
            emphasized = true,
        ) { CinemerickSummaryRowIcon(CinemerickIcons.Plus) }
    }
    summary.chains.forEach { chain ->
        CinemerickSummaryRow(
            title = bold(chain.chain.displayName()),
            subtitle = chain.venues.joinToString(", "),
            onClick = { onTargetClick(FilterTarget.ChainCard(chain.chain)) },
            onClickLabel = goTo,
        ) { CinemerickSummaryDot(chain.chain.badgeColors().first) }
    }

    CinemerickSummaryGroupLabel(stringResource(Res.string.summary_group_days_times))
    if (summary.days.isEmpty()) {
        CinemerickSummaryRow(
            title = AnnotatedString(stringResource(Res.string.summary_pick_day)),
            onClick = { onTargetClick(FilterTarget.Days) },
            onClickLabel = goTo,
            emphasized = true,
        ) { CinemerickSummaryRowIcon(CinemerickIcons.Plus) }
    }
    summary.days.forEach { day ->
        val range = stringResource(Res.string.time_range, day.startMinutes.toTimeString(), day.endMinutes.toTimeString())
        CinemerickSummaryRow(
            title = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(day.date.summaryDay()) }
                append(" · ")
                append(range)
            },
            onClick = { onTargetClick(FilterTarget.TimeWindow(day.date)) },
            onClickLabel = goTo,
        ) { CinemerickSummaryRowIcon(CinemerickIcons.Calendar) }
    }

    if (summary.hiddenFormats.isNotEmpty() || summary.filmQueries.isNotEmpty()) {
        CinemerickSummaryGroupLabel(stringResource(Res.string.summary_group_format_film))
    }
    if (summary.hiddenFormats.isNotEmpty()) {
        CinemerickSummaryRow(
            title = AnnotatedString(
                pluralStringResource(Res.plurals.summary_formats_hidden, summary.hiddenFormats.size, summary.hiddenFormats.joinToString(", "))
            ),
            onClick = { onTargetClick(FilterTarget.Formats) },
            onClickLabel = goTo,
        ) { CinemerickSummaryRowIcon(CinemerickIcons.Film) }
    }
    if (summary.filmQueries.isNotEmpty()) {
        CinemerickSummaryRow(
            title = AnnotatedString(stringResource(Res.string.summary_film_quoted, summary.filmQueries.joinToString(", "))),
            onClick = { onTargetClick(FilterTarget.FilmFilter) },
            onClickLabel = goTo,
            flag = if (summary.isFilmFilterChanged) stringResource(Res.string.summary_flag_changed) else null,
        ) { CinemerickSummaryRowIcon(CinemerickIcons.Search) }
    }

    if (summary.isFilmFilterChanged) {
        CinemerickPrimaryButton(
            text = stringResource(Res.string.summary_update_results),
            onClick = onUpdateClick,
            enabled = !summary.isSearching,
            loading = summary.isSearching,
            icon = CinemerickIcons.Search,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.space3),
        )
    }
}

/**
 * What a screen reader says when a jump lands on [this] target, e.g. "Notorious, Fiume Veneto" or
 * "Time window, Thursday 13/11, 20:00 – 24:00".
 */
@Composable
internal fun FilterTarget.spokenLabel(summary: FilterSummaryUi, filmFilter: String): String = when (this) {
    is FilterTarget.ChainCard -> listOf(
        chain.displayName(),
        summary.chains.firstOrNull { it.chain == chain }?.venues?.joinToString(", ") ?: stringResource(Res.string.chain_no_venue),
    ).joinToString(", ")
    FilterTarget.Cinemas -> stringResource(Res.string.cinemas_title)
    FilterTarget.Days -> stringResource(Res.string.days_title)
    is FilterTarget.TimeWindow -> {
        val day = summary.days.firstOrNull { it.date == date }
        listOfNotNull(
            stringResource(Res.string.time_window_title),
            date.dayLabel(),
            day?.let { stringResource(Res.string.time_range, it.startMinutes.toTimeString(), it.endMinutes.toTimeString()) },
        ).joinToString(", ")
    }
    FilterTarget.Formats -> listOfNotNull(
        stringResource(Res.string.formats_title),
        summary.hiddenFormats.takeIf { it.isNotEmpty() }?.let {
            pluralStringResource(Res.plurals.summary_formats_hidden, it.size, it.joinToString(", "))
        },
    ).joinToString(", ")
    FilterTarget.FilmFilter -> listOf(
        stringResource(Res.string.film_filter_title),
        filmFilter.ifBlank { stringResource(Res.string.summary_all_films) },
    ).joinToString(", ")
}

/** "Thu 13" / "Gio 13". */
@Composable
private fun LocalDate.summaryDay(): String = stringResource(Res.string.summary_day, stringResource(dayOfWeek.shortNameResource()), day)

private fun bold(text: String): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text) }
}

// Previews

private object SummaryPreviewData {
    val base = FilterSummaryUi(
        chains = listOf(
            ChainSummaryUi(Chain.THE_SPACE, listOf("Silea")),
            ChainSummaryUi(Chain.NOTORIOUS, listOf("Fiume Veneto")),
        ),
        venueCount = 2,
        days = listOf(
            DaySummaryUi(LocalDate(2026, 11, 13), 20 * 60, 24 * 60),
            DaySummaryUi(LocalDate(2026, 11, 14), 20 * 60, 24 * 60),
        ),
        hiddenFormats = listOf("3D"),
        filmQueries = listOf("Dune", "Wicked"),
    )
    val stale = base.copy(isFilmFilterChanged = true)
    val searching = stale.copy(isSearching = true)
    val missing = FilterSummaryUi()
    val many = base.copy(
        chains = listOf(
            ChainSummaryUi(Chain.THE_SPACE, listOf("Pordenone", "Silea")),
            ChainSummaryUi(Chain.UCI, listOf("UCI Luxe Marcon", "UCI Pradamano", "UCI Villesse")),
            ChainSummaryUi(Chain.NOTORIOUS, listOf("Fiume Veneto")),
            ChainSummaryUi(Chain.CRISTALLO, listOf("Oderzo")),
            ChainSummaryUi(Chain.CINEMAZERO, listOf("Pordenone")),
        ),
        venueCount = 8,
        days = (13..17).map { DaySummaryUi(LocalDate(2026, 11, it), (15 + it % 3) * 60, 24 * 60) },
        hiddenFormats = listOf("3D", "4DX"),
        filmQueries = listOf("Dune", "Wicked", "Zootropolis 2", "La grazia", "Avatar"),
    )
}

@Composable
private fun SummaryPreview(summary: FilterSummaryUi, expanded: Boolean = false) {
    FilterSummaryBar(
        summary = summary,
        expanded = expanded,
        onToggle = {},
        onTargetClick = {},
        onUpdateClick = {},
        onSettingsClick = {},
    )
}

@Preview(name = "Closed")
@Composable
private fun FilterSummaryBarPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.base) }
}

@Preview(name = "Closed, dark")
@Composable
private fun FilterSummaryBarDarkPreview() {
    DarkThemePreview { SummaryPreview(SummaryPreviewData.base) }
}

@Preview(name = "Open", heightDp = 720)
@Composable
private fun FilterSummaryBarOpenPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.base, expanded = true) }
}

@Preview(name = "Open, dark", heightDp = 720)
@Composable
private fun FilterSummaryBarOpenDarkPreview() {
    DarkThemePreview { SummaryPreview(SummaryPreviewData.base, expanded = true) }
}

@Preview(name = "Filters changed")
@Composable
private fun FilterSummaryBarStalePreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.stale) }
}

@Preview(name = "Filters changed, open", heightDp = 760)
@Composable
private fun FilterSummaryBarStaleOpenPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.stale, expanded = true) }
}

@Preview(name = "Searching")
@Composable
private fun FilterSummaryBarSearchingPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.searching) }
}

@Preview(name = "No venue, no day")
@Composable
private fun FilterSummaryBarMissingPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.missing) }
}

@Preview(name = "No venue, no day, open", heightDp = 520)
@Composable
private fun FilterSummaryBarMissingOpenPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.missing, expanded = true) }
}

@Preview(name = "Many filters")
@Composable
private fun FilterSummaryBarManyPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.many) }
}

@Preview(name = "Many filters, open", heightDp = 800)
@Composable
private fun FilterSummaryBarManyOpenPreview() {
    LightThemePreview { SummaryPreview(SummaryPreviewData.many, expanded = true) }
}
