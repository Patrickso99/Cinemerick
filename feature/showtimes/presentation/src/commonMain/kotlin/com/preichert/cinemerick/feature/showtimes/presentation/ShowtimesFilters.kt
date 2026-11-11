package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.getValue
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import com.preichert.cinemerick.core.designsystem.Motion
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.CinemerickTimeRangeSlider
import com.preichert.cinemerick.core.designsystem.LocalAnimationsEnabled
import com.preichert.cinemerick.core.designsystem.animateItemIfEnabled
import com.preichert.cinemerick.core.designsystem.CinemerickChainCard
import com.preichert.cinemerick.core.designsystem.CinemerickTimeText
import com.preichert.cinemerick.core.presentation.toTimeString
import com.preichert.cinemerick.feature.showtimes.domain.ParsedTime
import com.preichert.cinemerick.feature.showtimes.domain.parseTimeInput
import com.preichert.cinemerick.core.designsystem.CinemerickCard
import com.preichert.cinemerick.core.designsystem.CinemerickFilterChip
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickSecondaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_range
import org.jetbrains.compose.resources.stringResource
import com.preichert.cinemerick.feature.showtimes.presentation.resources.add_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinemas_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinemas_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.clear
import com.preichert.cinemerick.feature.showtimes.presentation.resources.days_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter_optional
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_selected_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_show_all
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.open_calendar
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_today
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_tomorrow
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_weekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.search_showtimes
import com.preichert.cinemerick.feature.showtimes.presentation.resources.chain_no_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_window_per_day
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_window_title
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource

internal fun LocalDate.shortDate(): String =
    "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"

/** Clear and Search, docked directly under the film search field (not floating). One primary action: Search. */
@Composable
internal fun SearchActionBar(
    canClear: Boolean,
    isLoading: Boolean,
    onClearClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CinemerickSecondaryButton(
            text = stringResource(Res.string.clear),
            onClick = onClearClick,
            enabled = canClear,
        )
        CinemerickPrimaryButton(
            text = stringResource(Res.string.search_showtimes),
            onClick = onSearchClick,
            enabled = !isLoading,
            loading = isLoading,
            icon = CinemerickIcons.Search,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Caption above a group: labelSmall, caps, onSurfaceVariant. */
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

/** Caption on the left, a secondary value or action on the right. */
@Composable
private fun SectionHeader(title: String, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SectionLabel(title)
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

/** The film filter. [onSearch] is the keyboard's search key. */
@Composable
internal fun FilmSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusManager: FocusManager = LocalFocusManager.current
    val colors = CinemerickTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        SectionHeader(title = stringResource(Res.string.film_filter_title)) {
            Text(
                text = stringResource(Res.string.film_filter_optional),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerHigh)
                .padding(horizontal = Spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space3)
        ) {
            Icon(
                painter = CinemerickIcons.Search,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.onSurface,
                ),
                singleLine = true,
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }),
                decorationBox = { innerField ->
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.film_filter),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    innerField()
                }
            )
            if (value.isNotEmpty()) {
                val clearLabel = stringResource(Res.string.clear)
                CinemerickIconButton(
                    icon = CinemerickIcons.Close,
                    contentDescription = clearLabel,
                    onClick = { onValueChange("") },
                    container = Color.Transparent,
                    tint = colors.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * One card per cinema chain, all eight, in the order of [Chain]. A card shows the chain badge, the selected
 * venues as a count, and expands in place to the selected venues plus "Add venue". Tap anywhere on the header to
 * expand. An empty chain shows a plus that opens the venue picker straight away. On wide layouts the chains are chips.
 */
@Composable
internal fun CinemasSection(
    selectedVenues: Set<Venue>,
    expandedChains: Set<Chain>,
    isWideScreen: Boolean,
    onChainExpandToggle: (Chain) -> Unit,
    onVenuePickerOpen: (Chain) -> Unit,
    onVenueRemove: (Venue) -> Unit
) {
    val addVenue = stringResource(Res.string.add_venue)
    val noVenue = stringResource(Res.string.chain_no_venue)
    val colors = CinemerickTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        SectionHeader(title = stringResource(Res.string.cinemas_title)) {
            Text(
                text = stringResource(Res.string.cinemas_count, selectedVenues.size),
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
        }
        if (isWideScreen) {
            WideChainChips(selectedVenues = selectedVenues, onChainClick = onVenuePickerOpen)
        } else Chain.entries.forEach { chain ->
            val (container, onContainer) = chain.badgeColors()
            val venuesOfChain = selectedVenues.filter { it.chain == chain }
            CinemerickChainCard(
                name = chain.displayName(),
                subtitle = if (venuesOfChain.isEmpty()) noVenue else venuesOfChain.joinToString { it.name },
                initials = chain.shortName(),
                badgeContainer = container,
                badgeContent = onContainer,
                selectedCount = venuesOfChain.size,
                expanded = chain in expandedChains,
                onToggle = { onChainExpandToggle(chain) },
                onQuickAdd = { onVenuePickerOpen(chain) },
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                    verticalArrangement = Arrangement.spacedBy(Spacing.space2)
                ) {
                    venuesOfChain.forEach { venue ->
                        CinemerickFilterChip(
                            label = venue.name,
                            selected = true,
                            onClick = { onVenueRemove(venue) },
                        )
                    }
                    CinemerickFilterChip(
                        label = addVenue,
                        selected = false,
                        leadingIcon = CinemerickIcons.Plus,
                        onClick = { onVenuePickerOpen(chain) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun FormatsSection(
    availableTags: List<String>,
    hiddenTags: Set<String>,
    onFormatToggle: (String) -> Unit,
    onShowAllClick: () -> Unit
) {
    val visibleCount = availableTags.count { it !in hiddenTags }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        SectionHeader(title = stringResource(Res.string.formats_title)) {
            Text(
                text = stringResource(Res.string.formats_selected_count, visibleCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hiddenTags.isNotEmpty()) {
                Spacer(Modifier.width(Spacing.space2))
                Text(
                    text = stringResource(Res.string.formats_show_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onShowAllClick)
                        .padding(horizontal = Spacing.space2, vertical = 12.dp)
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            verticalArrangement = Arrangement.spacedBy(Spacing.space2)
        ) {
            availableTags.forEach { tag ->
                CinemerickFilterChip(
                    label = tag,
                    selected = tag !in hiddenTags,
                    onClick = { onFormatToggle(tag) },
                )
            }
        }
    }
}

private const val DAY_STRIP_LENGTH = 6
private val DAY_TILE_WIDTH = 64.dp

/**
 * The days to search: quick picks, then a strip of day tiles on phones or the month grid on wide layouts.
 * [calendarMonth] is the first day of the month that grid shows.
 */
@Composable
internal fun DaysSection(
    days: List<DayUi>,
    today: LocalDate,
    isWideScreen: Boolean,
    calendarMonth: LocalDate,
    onCalendarOpen: () -> Unit,
    onTodayClick: () -> Unit,
    onTomorrowClick: () -> Unit,
    onWeekendClick: () -> Unit,
    onDayToggle: (LocalDate) -> Unit,
    onMonthChange: (LocalDate) -> Unit
) {
    // Today and the following days, plus any chosen day outside that window so every selection stays visible.
    val stripDates = remember(today, days) {
        val upcoming: List<LocalDate> = (0 until DAY_STRIP_LENGTH).map { offset -> today.plus(DatePeriod(days = offset)) }
        val chosen: List<LocalDate> = days.filter { it.isSelected }.map { it.date }
        (upcoming + chosen).distinct().sorted()
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        SectionHeader(title = stringResource(Res.string.days_title)) {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(role = Role.Button, onClick = onCalendarOpen)
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = Spacing.space2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
            ) {
                Icon(
                    painter = CinemerickIcons.Calendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(Res.string.open_calendar),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        // Quick picks: actions, not toggles, so they carry no selected state.
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            CinemerickFilterChip(label = stringResource(Res.string.quick_today), selected = false, onClick = onTodayClick)
            CinemerickFilterChip(label = stringResource(Res.string.quick_tomorrow), selected = false, onClick = onTomorrowClick)
            CinemerickFilterChip(label = stringResource(Res.string.quick_weekend), selected = false, onClick = onWeekendClick)
        }
        if (isWideScreen) {
            // Two-pane layouts: the month grid inline, as in docs/design (Large, desktop). Past days are disabled.
            CinemerickCard(modifier = Modifier.fillMaxWidth()) {
                MultiDateCalendar(
                    selected = days.filter { it.isSelected }.mapTo(mutableSetOf()) { it.date },
                    minDate = today,
                    shownMonth = calendarMonth,
                    onToggle = onDayToggle,
                    onMonthChange = onMonthChange,
                    modifier = Modifier.padding(Spacing.space3),
                )
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                items(stripDates, key = { it.toString() }) { date ->
                    DayTile(
                        date = date,
                        selected = days.any { it.date == date && it.isSelected },
                        isToday = date == today,
                        onClick = { onDayToggle(date) },
                        modifier = animateItemIfEnabled()
                    )
                }
            }
        }
    }
}

/** Day tile, 64x84dp, shape md. Selected = primaryContainer; today (not selected) = a 5dp primary dot. */
@Composable
internal fun DayTile(
    date: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CinemerickTheme.colors
    val container by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surfaceContainer,
        tween(Motion.duration(LocalAnimationsEnabled.current, Motion.Press)),
        label = "dayTileContainer",
    )
    val contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    Column(
        modifier = modifier
            .size(width = 64.dp, height = 84.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(vertical = Spacing.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.chipDayName(),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
        )
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = contentColor
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isToday && !selected) colors.primary else Color.Transparent)
        )
    }
}

/** The window slider covers the whole day in 15-minute steps; 1440 minutes is "24:00". */
private const val DAY_MINUTES = 1440
private const val SLIDER_STEP_MINUTES = 15
// "24:00" is stored as the day's last minute (23:59), so it is read back as the end of the slider.
private const val LAST_MINUTE_OF_DAY = 23 * 60 + 59

/** One time range per selected day. [onRangeChange] gets the day and its new start and end, as "HH:mm". */
@Composable
internal fun TimeWindowSection(
    days: List<DayUi>,
    onRangeChange: (date: LocalDate, minTime: String, maxTime: String) -> Unit
) {
    val selectedDays = days.filter { it.isSelected }
    if (selectedDays.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2)
    ) {
        SectionHeader(title = stringResource(Res.string.time_window_title)) {
            Text(
                text = stringResource(Res.string.time_window_per_day),
                style = MaterialTheme.typography.labelMedium,
                color = CinemerickTheme.colors.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(CinemerickTheme.colors.surfaceContainer)
                .padding(horizontal = Spacing.space3, vertical = Spacing.space3),
            verticalArrangement = Arrangement.spacedBy(Spacing.space3)
        ) {
            selectedDays.forEach { day ->
                DayRangeRow(day = day, onRangeChange = { minTime, maxTime -> onRangeChange(day.date, minTime, maxTime) })
            }
        }
    }
}

@Composable
internal fun DayRangeRow(
    day: DayUi,
    onRangeChange: (minTime: String, maxTime: String) -> Unit
) {
    val colors = CinemerickTheme.colors
    // A blank bound means "no limit", so it sits at the edge of the day until the user moves the thumb.
    val start = day.minTime.toMinutesOr(default = 0)
    val end = day.maxTime.toMinutesOr(default = DAY_MINUTES).let { if (it == LAST_MINUTE_OF_DAY) DAY_MINUTES else it }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.space1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.space1),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.date.dayLabel(),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface
            )
            CinemerickTimeText(text = stringResource(Res.string.time_range, start.toTimeString(), end.toTimeString()))
        }
        CinemerickTimeRangeSlider(
            startMinutes = start,
            endMinutes = end,
            onChange = { newStart, newEnd -> onRangeChange(newStart.toTimeString(), newEnd.toTimeString()) },
        )
    }
}

private fun String.toMinutesOr(default: Int): Int {
    val time = (parseTimeInput(this) as? ParsedTime.Valid)?.value ?: return default
    return time.hour * 60 + time.minute
}

// Previews - Light theme

@Preview
@Composable
private fun DayTilePreviewLight() {
    LightThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            DayTile(date = LocalDate(2026, 10, 16), selected = true, isToday = false, onClick = {})
            DayTile(date = LocalDate(2026, 10, 17), selected = false, isToday = true, onClick = {})
        }
    }
}

@Preview
@Composable
private fun DayRangeRowPreviewLight() {
    LightThemePreview {
        Column {
            DayRangeRow(day = PreviewData.dayUiSelected(), onRangeChange = { _, _ -> })
            Spacer(modifier = Modifier.height(Spacing.space4))
            DayRangeRow(day = PreviewData.dayUiWithTimeError(), onRangeChange = { _, _ -> })
        }
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun DayTilePreviewDark() {
    DarkThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            DayTile(date = LocalDate(2026, 10, 16), selected = true, isToday = false, onClick = {})
            DayTile(date = LocalDate(2026, 10, 17), selected = false, isToday = true, onClick = {})
        }
    }
}

@Preview
@Composable
private fun DayRangeRowPreviewDark() {
    DarkThemePreview {
        Column {
            DayRangeRow(day = PreviewData.dayUiSelected(), onRangeChange = { _, _ -> })
            Spacer(modifier = Modifier.height(Spacing.space4))
            DayRangeRow(day = PreviewData.dayUiWithTimeError(), onRangeChange = { _, _ -> })
        }
    }
}

/**
 * Two-pane layouts: the eight cinemas as chips, as in docs/design (Large). A chip with a selection shows its color,
 * name and count. Tapping any chip opens the venue picker for that cinema.
 */
@Composable
private fun WideChainChips(selectedVenues: Set<Venue>, onChainClick: (Chain) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Chain.entries.forEach { chain ->
            val count = selectedVenues.count { it.chain == chain }
            CinemerickFilterChip(
                label = if (count > 0) "${chain.displayName()} $count" else chain.displayName(),
                selected = count > 0,
                leadingDot = chain.badgeColors().first,
                onClick = { onChainClick(chain) },
            )
        }
    }
}
