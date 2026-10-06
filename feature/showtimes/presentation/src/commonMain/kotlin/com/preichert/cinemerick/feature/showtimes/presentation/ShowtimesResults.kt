package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.presentation.currentPlatform
import com.preichert.cinemerick.core.presentation.util.DeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.cancel
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_results
import com.preichert.cinemerick.feature.showtimes.presentation.resources.open_cinema_app
import com.preichert.cinemerick.feature.showtimes.presentation.resources.options_count
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

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
                        repeat(columns - rowGroups.size) { Spacer(Modifier.weight(1f)) }
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
internal fun FilmGroupCard(group: FilmGroupUi, large: Boolean) {
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

@Composable
internal fun CinemaAppBadge(venue: Venue, container: Color, content: Color) {
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
internal fun ShowingRow(showing: ShowingUi) {
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
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:00", PreviewData.sileaVenue, "2D · VO"))
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:30", PreviewData.marconVenue, null))
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
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:00", PreviewData.sileaVenue, "2D · VO"))
            ShowingRow(showing = ShowingUi("Giovedì", "01/10", "21:30", PreviewData.marconVenue, null))
        }
    }
}
