package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.pressScale
import com.preichert.cinemerick.core.designsystem.TimeTextField
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.add_venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cinemas_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.clear
import com.preichert.cinemerick.feature.showtimes.presentation.resources.days_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.fetch
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_filter
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_show_all
import com.preichert.cinemerick.feature.showtimes.presentation.resources.formats_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.pick_days
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_today
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_tomorrow
import com.preichert.cinemerick.feature.showtimes.presentation.resources.quick_weekend
import com.preichert.cinemerick.feature.showtimes.presentation.resources.range_from
import com.preichert.cinemerick.feature.showtimes.presentation.resources.range_to
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_error
import com.preichert.cinemerick.feature.showtimes.presentation.resources.time_placeholder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

internal fun LocalDate.shortDate(): String =
    "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}"

@Composable
internal fun ColumnScope.FiltersSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit
) {
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
                androidx.compose.foundation.layout.FlowRow(
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
            FilledTonalButton(
                onClick = { onAction(ShowtimesAction.OnCalendarOpen) },
                modifier = Modifier.padding(horizontal = Spacing.lg)
            ) {
                Text(stringResource(Res.string.pick_days))
            }
            if (state.days.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(state.days, key = { it.date.toString() }) { day ->
                        DayChip(
                            day = day,
                            onClick = { onAction(ShowtimesAction.OnDayToggle(day.date)) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    state.days.filter { it.isSelected }.forEach { day ->
        SectionCard(modifier = Modifier.fillMaxWidth()) {
            DayRangeRow(day = day, onAction = onAction)
        }
    }

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

    val generateSource = remember { MutableInteractionSource() }
    Button(
        onClick = { onAction(ShowtimesAction.OnGenerateClick) },
        enabled = !state.isLoading,
        shape = MaterialTheme.shapes.large,
        interactionSource = generateSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .pressScale(generateSource)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = Spacing.sm).size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        }
        Text(stringResource(Res.string.fetch), style = MaterialTheme.typography.titleMedium)
    }

    val clearSource = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = { onAction(ShowtimesAction.OnClearClick) },
        enabled = state.canClear,
        shape = MaterialTheme.shapes.large,
        interactionSource = clearSource,
        modifier = Modifier.fillMaxWidth().pressScale(clearSource)
    ) {
        Text(stringResource(Res.string.clear))
    }
}

@Composable
internal fun DayChip(day: DayUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    FilterChip(
        selected = day.isSelected,
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.height(56.dp).pressScale(interactionSource),
        shape = MaterialTheme.shapes.medium,
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = Spacing.xs)
            ) {
                Text(
                    text = day.date.chipDayName(),
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

@Composable
internal fun DayRangeRow(
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
                text = day.date.dayLabel(),
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

// Previews - Light theme

@Preview
@Composable
private fun DayChipPreviewLight() {
    LightThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            DayChip(day = PreviewData.dayUiSelected(), onClick = {})
            DayChip(day = PreviewData.dayUiNotSelected(), onClick = {})
        }
    }
}

@Preview
@Composable
private fun DayRangeRowPreviewLight() {
    LightThemePreview {
        Column {
            DayRangeRow(day = PreviewData.dayUiSelected(), onAction = {})
            Spacer(modifier = Modifier.height(Spacing.lg))
            DayRangeRow(day = PreviewData.dayUiWithTimeError(), onAction = {})
        }
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun DayChipPreviewDark() {
    DarkThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            DayChip(day = PreviewData.dayUiSelected(), onClick = {})
            DayChip(day = PreviewData.dayUiNotSelected(), onClick = {})
        }
    }
}

@Preview
@Composable
private fun DayRangeRowPreviewDark() {
    DarkThemePreview {
        Column {
            DayRangeRow(day = PreviewData.dayUiSelected(), onAction = {})
            Spacer(modifier = Modifier.height(Spacing.lg))
            DayRangeRow(day = PreviewData.dayUiWithTimeError(), onAction = {})
        }
    }
}
