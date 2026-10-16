package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.designsystem.CinemerickChainChip
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.CinemerickTimeText
import com.preichert.cinemerick.core.designsystem.CinemerickTonalButton
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.animateItemIfEnabled
import com.preichert.cinemerick.core.designsystem.bounceClickable
import com.preichert.cinemerick.core.designsystem.staggeredAppear
import com.preichert.cinemerick.core.presentation.util.DeviceConfiguration
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.buy_ticket
import com.preichert.cinemerick.feature.showtimes.presentation.resources.copy_poll
import com.preichert.cinemerick.feature.showtimes.presentation.resources.film_details
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate_hint
import com.preichert.cinemerick.feature.showtimes.presentation.resources.no_results
import com.preichert.cinemerick.feature.showtimes.presentation.resources.options_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.results_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.sold_out
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val log = Logger.withTag("ShowtimesResults")

private val MIN_GRID_CARD_WIDTH = 340.dp

// Phone portrait: one lazy item per card, so only the visible film cards are composed.
internal fun LazyListScope.resultsItems(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit
) {
    state.venueErrors.forEachIndexed { index, error ->
        item(key = "error-$index", contentType = "error") {
            ResultItem(animateItemIfEnabled()) { VenueErrorBanner(error) }
        }
    }

    if (state.isLoading) {
        items(3, key = { "skeleton-$it" }, contentType = { "skeleton" }) {
            ResultItem { SkeletonCard() }
        }
        return
    }

    if (!state.hasGenerated) {
        item(key = "hint", contentType = "message") {
            ResultItem(animateItemIfEnabled()) { MessageCard(icon = CinemerickIcons.Film, text = stringResource(Res.string.generate_hint)) }
        }
    } else if (state.groups.isEmpty()) {
        item(key = "empty", contentType = "message") {
            ResultItem(animateItemIfEnabled()) { MessageCard(icon = CinemerickIcons.Film, text = stringResource(Res.string.no_results)) }
        }
    }

    if (state.pollText.isNotEmpty()) {
        item(key = "copy", contentType = "copy") {
            ResultItem(animateItemIfEnabled()) { ResultsSummary(state, onCopy = null) }
        }
    }

    items(state.groups, key = { it.title }, contentType = { "film" }) { group ->
        ResultItem(animateItemIfEnabled()) {
            FilmGroupCard(
                group = group,
                large = false,
                onDetailsClick = { onAction(ShowtimesAction.OnFilmDetailsOpen(group.title)) }
            )
        }
    }
}

@Composable
private fun ResultItem(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = Spacing.space4)
            .padding(bottom = Spacing.space3)
    ) { content() }
}

@Composable
private fun VenueErrorBanner(error: VenueErrorUi) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(Spacing.space3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2)
        ) {
            Icon(
                painter = painterResource(CinemerickIcons.Alert),
                contentDescription = null,
                tint = colors.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${error.venueName}: ${error.message.asString()}",
                color = colors.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * "Results" heading with the showing count and timing. On wide layouts [onCopy] adds the Copy poll button in the
 * header; on phones Copy poll is the floating action instead, so the caller passes null.
 */
@Composable
private fun ResultsSummary(state: ShowtimesState, onCopy: (() -> Unit)?) {
    val colors = CinemerickTheme.colors
    val elapsed = state.elapsedTimeMillis?.let { millis -> "${millis / 1000}.${(millis % 1000).toString().padStart(3, '0')}s" }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
            Text(
                text = stringResource(Res.string.results_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
            Text(
                text = listOfNotNull(stringResource(Res.string.options_count, state.optionsCount), elapsed).joinToString(" · "),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant
            )
        }
        if (onCopy != null) {
            CinemerickPrimaryButton(
                text = stringResource(Res.string.copy_poll),
                onClick = onCopy,
                icon = painterResource(CinemerickIcons.Copy),
            )
        }
    }
}

@Composable
internal fun ColumnScope.ResultsSection(
    state: ShowtimesState,
    onAction: (ShowtimesAction) -> Unit,
    configuration: DeviceConfiguration
) {
    state.venueErrors.forEach { error -> VenueErrorBanner(error) }

    if (state.isLoading) {
        repeat(3) { SkeletonCard() }
        return
    }

    if (!state.hasGenerated) {
        MessageCard(icon = CinemerickIcons.Film, text = stringResource(Res.string.generate_hint))
    } else if (state.groups.isEmpty()) {
        MessageCard(icon = CinemerickIcons.Film, text = stringResource(Res.string.no_results))
    }

    if (state.pollText.isNotEmpty()) {
        ResultsSummary(state, onCopy = null)
    }

    if (configuration.isMobile) {
        state.groups.forEachIndexed { index, group ->
            AnimatedFilmGroupCard(group, large = false, index = index, onDetailsClick = { onAction(ShowtimesAction.OnFilmDetailsOpen(group.title)) })
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = (maxWidth / MIN_GRID_CARD_WIDTH).toInt().coerceIn(1, 2)
            // Masonry: independent columns so cards stack without vertical gaps
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
                verticalAlignment = Alignment.Top
            ) {
                repeat(columns) { colIndex ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.space3)
                    ) {
                        state.groups.forEachIndexed { index, group ->
                            if (index % columns == colIndex) {
                                AnimatedFilmGroupCard(
                                    group = group,
                                    large = true,
                                    index = index,
                                    onDetailsClick = { onAction(ShowtimesAction.OnFilmDetailsOpen(group.title)) }
                                )
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
    index: Int = 0,
    onDetailsClick: () -> Unit = {}
) {
    // Keyed on the group so a refreshed result list replays the cascade
    key(group) {
        Box(modifier = modifier.staggeredAppear(index)) {
            FilmGroupCard(group, large = large, onDetailsClick = onDetailsClick)
        }
    }
}

/**
 * Film card: poster first (104x156 on phone, 132 on wide layouts), then the title, the details button and the
 * showtimes grouped by day. Surface only, no outline and no shadow.
 */
@Composable
internal fun FilmGroupCard(group: FilmGroupUi, large: Boolean, onDetailsClick: () -> Unit = {}) {
    val colors = CinemerickTheme.colors
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(colors.surfaceContainer)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.space3)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
                group.posterUrl?.let {
                    PosterImage(
                        url = it,
                        contentDescription = group.title,
                        width = 104.dp
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.space2),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = group.title,
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CinemerickTonalButton(
                        text = stringResource(Res.string.film_details),
                        onClick = onDetailsClick,
                        icon = painterResource(CinemerickIcons.ChevronRight),
                    )
                }
            }
            group.showings.groupBy { it.date }.entries.forEachIndexed { index, (date, showingsForDate) ->
                val dayName = showingsForDate.firstOrNull()?.day?.fullName() ?: ""
                DayHeader(dayName = dayName, date = date, count = showingsForDate.size, first = index == 0)
                // Rows of one day sit flush; a hairline separates the rows, not the day heading.
                Column {
                    showingsForDate.forEachIndexed { rowIndex, showing ->
                        ShowingRow(showing, withDivider = rowIndex > 0)
                    }
                }
            }
        }
    }
}

/** Day heading shown once per day: the date in bold on the left, the showing count on the right. */
@Composable
private fun DayHeader(dayName: String, date: String, count: Int, first: Boolean) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (first) Spacing.space1 else Spacing.space4, bottom = Spacing.space1),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$dayName $date",
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface
        )
        Text(
            text = stringResource(Res.string.options_count, count),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
    }
}

@Composable
internal fun PosterImage(url: String, contentDescription: String, width: Dp = 104.dp) {
    var isEnlarged by remember { mutableStateOf(false) }
    val placeholder = CinemerickTheme.colors.surfaceContainerHigh

    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .width(width)
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.medium)
            .background(placeholder)
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

/**
 * One showing, in fixed columns: time (48dp, tabular) · chain chip · format tag · sold out · ticket.
 * The ticket is a 48dp icon button with a content description.
 */
@Composable
internal fun ShowingRow(showing: ShowingUi, withDivider: Boolean = false) {
    val uriHandler = LocalUriHandler.current
    val colors = CinemerickTheme.colors
    val (chainContainer, chainContent) = showing.venue.chain.badgeColors()
    Column {
        if (withDivider) HorizontalDivider(color = colors.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2)
        ) {
            CinemerickTimeText(
                text = showing.time,
                color = if (showing.soldOut) colors.onSurfaceVariant else colors.onSurface,
                modifier = Modifier.width(48.dp),
            )
            CinemerickChainChip(
                label = showing.venue.name,
                container = chainContainer,
                content = chainContent,
                modifier = Modifier.widthIn(max = 180.dp),
            )
            showing.format?.let { FormatTag(text = it) }
            Spacer(modifier = Modifier.weight(1f))
            if (showing.soldOut) {
                Badge(
                    text = stringResource(Res.string.sold_out),
                    container = colors.primaryContainer,
                    content = colors.onPrimaryContainer
                )
            }
            showing.ticketUrl?.let { url ->
                CinemerickIconButton(
                    icon = painterResource(CinemerickIcons.Cart),
                    contentDescription = stringResource(Res.string.buy_ticket),
                    onClick = {
                        log.i { "Opening purchase page for ${showing.venue.name} ${showing.date} ${showing.time}: $url" }
                        uriHandler.openUri(url)
                    },
                )
            }
        }
    }
}

@Composable
private fun FormatTag(text: String) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(CinemerickTheme.colors.surfaceContainerHigh)
            .padding(horizontal = Spacing.space2),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CinemerickTheme.colors.onSurfaceVariant
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
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            ShowingRow(showing = ShowingUi(DayOfWeek.THURSDAY, "01/10", "21:00", PreviewData.sileaVenue, "2D · VO", "https://example.com/buy"))
            ShowingRow(showing = ShowingUi(DayOfWeek.THURSDAY, "01/10", "21:30", PreviewData.marconVenue, null), withDivider = true)
            ShowingRow(showing = ShowingUi(DayOfWeek.THURSDAY, "01/10", "22:00", PreviewData.sileaVenue, "2D", "https://example.com/buy", soldOut = true), withDivider = true)
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
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            ShowingRow(showing = ShowingUi(DayOfWeek.THURSDAY, "01/10", "21:00", PreviewData.sileaVenue, "2D · VO", "https://example.com/buy"))
            ShowingRow(showing = ShowingUi(DayOfWeek.THURSDAY, "01/10", "21:30", PreviewData.marconVenue, null), withDivider = true)
            ShowingRow(showing = ShowingUi(DayOfWeek.THURSDAY, "01/10", "22:00", PreviewData.sileaVenue, "2D", "https://example.com/buy", soldOut = true), withDivider = true)
        }
    }
}
