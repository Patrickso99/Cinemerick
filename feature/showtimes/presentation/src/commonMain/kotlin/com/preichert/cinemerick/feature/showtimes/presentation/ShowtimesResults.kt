package com.preichert.cinemerick.feature.showtimes.presentation

import co.touchlab.kermit.Logger
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.TicketActionColors
import com.preichert.cinemerick.core.designsystem.bounceClickable
import com.preichert.cinemerick.core.designsystem.pressScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.preichert.cinemerick.core.designsystem.staggeredAppear
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.presentation.util.DeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_results
import com.preichert.cinemerick.feature.showtimes.presentation.resources.buy_ticket
import com.preichert.cinemerick.feature.showtimes.presentation.resources.sold_out
import com.preichert.cinemerick.feature.showtimes.presentation.resources.options_count
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

private val log = Logger.withTag("ShowtimesResults")

private val MIN_GRID_CARD_WIDTH = 340.dp

@Composable
internal fun ColumnScope.ResultsSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    configuration: DeviceConfiguration
) {
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

    if (state.isLoading) {
        repeat(3) { SkeletonCard() }
        return
    }

    if (!state.hasGenerated) {
        MessageCard(emoji = "🎬", text = stringResource(Res.string.generate_hint))
    } else if (state.groups.isEmpty()) {
        MessageCard(emoji = "🍿", text = stringResource(Res.string.no_results))
    }

    if (state.pollText.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            val copySource = remember { MutableInteractionSource() }
            FilledTonalButton(
                onClick = { onAction(ShowtimesAction.OnCopyClick) },
                shape = MaterialTheme.shapes.large,
                interactionSource = copySource,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .pressScale(copySource)
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

    if (configuration.isMobile) {
        state.groups.forEachIndexed { index, group -> AnimatedFilmGroupCard(group, large = false, index = index) }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = (maxWidth / MIN_GRID_CARD_WIDTH).toInt().coerceAtLeast(1)
            // Masonry: independent columns so cards stack without vertical gaps
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.Top
            ) {
                repeat(columns) { colIndex ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        state.groups.forEachIndexed { index, group ->
                            if (index % columns == colIndex) {
                                AnimatedFilmGroupCard(group = group, large = true, index = index)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AnimatedFilmGroupCard(
    group: FilmGroupUi,
    large: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    // Keyed on the group so a refreshed result list replays the cascade
    key(group) {
        Box(modifier = modifier.staggeredAppear(index)) {
            FilmGroupCard(group, large = large)
        }
    }
}

@Composable
internal fun FilmGroupCard(group: FilmGroupUi, large: Boolean) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
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
internal fun PosterImage(url: String, contentDescription: String, width: Dp = 96.dp) {
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
            .bounceClickable { isEnlarged = true }
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
                    .bounceClickable { isEnlarged = false }
            )
        }
    }
}

@Composable
internal fun ShowingRow(showing: ShowingUi) {
    val uriHandler = LocalUriHandler.current
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        itemVerticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Badge(
            text = showing.time,
            container = if (showing.soldOut) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
            content = if (showing.soldOut) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
            bold = true
        )
        if (showing.soldOut) {
            Badge(
                text = stringResource(Res.string.sold_out),
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        val (container, content) = showing.venue.chain.badgeColors()
        Badge(text = showing.venue.name, container = container, content = content)
        showing.format?.let {
            Badge(
                text = it,
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        showing.ticketUrl?.let { url ->
            TicketButton(onClick = {
                log.i { "Opening purchase page for ${showing.venue.name} ${showing.date} ${showing.time}: $url" }
                uriHandler.openUri(url)
            })
        }
    }
}

@Composable
private fun TicketButton(onClick: () -> Unit) {
    val description = stringResource(Res.string.buy_ticket)
    Surface(
        shape = MaterialTheme.shapes.small,
        color = TicketActionColors.container,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .semantics { contentDescription = description }
            .bounceClickable(role = Role.Button, onClick = onClick)
    ) {
        Text(
            text = "\uD83D\uDED2",
            style = MaterialTheme.typography.titleMedium,
            color = TicketActionColors.content,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
        )
    }
}

// Previews - Light theme

@Preview
@Composable
private fun FilmGroupCardPreviewLight() {
    LightThemePreview {
        FilmGroupCard(group = PreviewData.filmGroupWithPoster(), large = false)
    }
}

@Preview
@Composable
private fun ShowingRowPreviewLight() {
    LightThemePreview {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:00", PreviewData.sileaVenue, "2D · VO", "https://example.com/buy"))
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:30", PreviewData.marconVenue, null))
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "22:00", PreviewData.sileaVenue, "2D", "https://example.com/buy", soldOut = true))
        }
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun FilmGroupCardPreviewDark() {
    DarkThemePreview {
        FilmGroupCard(group = PreviewData.filmGroupWithPoster(), large = false)
    }
}

@Preview
@Composable
private fun ShowingRowPreviewDark() {
    DarkThemePreview {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:00", PreviewData.sileaVenue, "2D · VO", "https://example.com/buy"))
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:30", PreviewData.marconVenue, null))
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "22:00", PreviewData.sileaVenue, "2D", "https://example.com/buy", soldOut = true))
        }
    }
}
