package com.preichert.cinemerick.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Share of the window height the open panel may take before it scrolls. */
private const val PanelMaxHeightFraction = 0.6f

/** Most chain dots drawn before the "+N" count. */
const val SummaryDotsMax = 4

/**
 * Summary bar pinned at the top of a scrolling screen (docs/design, FilterSummaryBar). Closed it is one button with two
 * lines ([title], [subtitle]) and a chevron, followed by [trailing] (a 48dp action). Open, it grows down into [panel]
 * with shape xl at the bottom; the caller draws the scrim below it.
 *
 * @param description what a screen reader says for the summary button: the whole summary, not the abbreviations
 * @param toggleLabel the action read with it, e.g. "Show all filters" / "Hide filters"
 * @param barModifier applied to the bar row only (not the panel), e.g. to mark it as the area a jump lands below
 * @param emphasizedSubtitle the subtitle is a call to action (primary, with a dot), e.g. "Filters changed"
 * @param busy a search is running: a spinner before the subtitle and a linear progress on the bottom edge
 */
@Composable
fun CinemerickSummaryBar(
    title: AnnotatedString,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    description: String,
    toggleLabel: String,
    modifier: Modifier = Modifier,
    barModifier: Modifier = Modifier,
    emphasizedSubtitle: Boolean = false,
    busy: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    panel: @Composable ColumnScope.() -> Unit,
) {
    val colors = CinemerickTheme.colors
    val animationsEnabled = LocalAnimationsEnabled.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(Motion.duration(animationsEnabled, Motion.Sheet)),
        label = "summaryChevron",
    )
    val panelMaxHeight = with(LocalDensity.current) { (LocalWindowInfo.current.containerSize.height * PanelMaxHeightFraction).toDp() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        shape = if (expanded) RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp) else RectangleShape,
        shadowElevation = if (expanded) 8.dp else 3.dp,
    ) {
        Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Box(modifier = barModifier) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.space2),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space1),
                ) {
                    Surface(
                        onClick = onToggle,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .semantics {
                                contentDescription = description
                                onClick(label = toggleLabel) { onToggle(); true }
                            },
                        shape = MaterialTheme.shapes.medium,
                        color = colors.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.space3, vertical = Spacing.space2),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    leading?.invoke()
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    when {
                                        busy -> CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.primary)
                                        emphasizedSubtitle -> Box(Modifier.size(8.dp).background(colors.primary, CircleShape))
                                    }
                                    Text(
                                        text = subtitle,
                                        style = if (emphasizedSubtitle || busy) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                                        color = if (emphasizedSubtitle || busy) colors.primary else colors.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Icon(
                                painter = CinemerickIcons.ChevronUp,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = chevronRotation },
                            )
                        }
                    }
                    trailing()
                }
                if (busy) {
                    LinearProgressIndicator(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                        color = colors.primary,
                        trackColor = colors.surfaceContainerHigh,
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = if (animationsEnabled) expandVertically(tween(Motion.Sheet)) + fadeIn(tween(Motion.ListEnter)) else EnterTransition.None,
                exit = if (animationsEnabled) shrinkVertically(tween(Motion.Sheet)) + fadeOut(tween(Motion.ListEnter)) else ExitTransition.None,
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = panelMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(start = Spacing.space4, end = Spacing.space4, bottom = Spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    content = panel,
                )
            }
        }
    }
}

/** Group caption inside the summary panel: labelSmall, caps, onSurfaceVariant. */
@Composable
fun CinemerickSummaryGroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = CinemerickTheme.colors.onSurfaceVariant,
        modifier = modifier.padding(start = Spacing.space1, top = Spacing.space3, bottom = Spacing.space1),
    )
}

/**
 * One filter in the summary panel; tapping it jumps to the component. 48dp minimum, surfaceContainer, shape sm.
 *
 * @param subtitle second line, e.g. the venues under the chain name
 * @param flag short tag on the right, e.g. "Changed"
 * @param emphasized the row is the next thing to do ("Pick at least one venue"): primary, bold
 */
@Composable
fun CinemerickSummaryRow(
    title: AnnotatedString,
    onClick: () -> Unit,
    onClickLabel: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    flag: String? = null,
    emphasized: Boolean = false,
    leading: @Composable () -> Unit,
) {
    val colors = CinemerickTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics { onClick(label = onClickLabel) { onClick(); true } },
        shape = MaterialTheme.shapes.small,
        color = colors.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.space3, vertical = Spacing.space2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
        ) {
            leading()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (emphasized) FontWeight.Bold else null,
                    color = if (emphasized) colors.primary else colors.onSurface,
                )
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            if (flag != null) {
                Text(
                    text = flag,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier
                        .background(colors.primaryContainer, CircleShape)
                        .padding(horizontal = Spacing.space2, vertical = 2.dp),
                )
            }
            Icon(
                painter = CinemerickIcons.ArrowUpRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/** Leading icon of a [CinemerickSummaryRow]. Decorative: the row's text says what it is. */
@Composable
fun CinemerickSummaryRowIcon(painter: androidx.compose.ui.graphics.painter.Painter, modifier: Modifier = Modifier) {
    Icon(painter = painter, contentDescription = null, tint = CinemerickTheme.colors.onSurfaceVariant, modifier = modifier.size(18.dp))
}

/** A brand-color dot, 12dp in panel rows (decorative: the name is always written next to it). Dark theme adds the chainEdge stroke. */
@Composable
fun CinemerickSummaryDot(color: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
            .border(1.dp, CinemerickTheme.colors.chainEdge, CircleShape),
    )
}

/** Overlapping brand dots before the bar title: at most [SummaryDotsMax], then "+[more]". Decorative. */
@Composable
fun CinemerickSummaryDots(colors: List<Color>, more: Int, modifier: Modifier = Modifier) {
    if (colors.isEmpty()) return
    // 14dp dots overlapping by 4dp; the 2dp ring in the container color keeps them apart.
    Row(
        modifier = modifier.padding(end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy((-4).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        colors.forEach { color ->
            CinemerickSummaryDot(
                color = color,
                size = 14.dp,
                modifier = Modifier.border(2.dp, CinemerickTheme.colors.surfaceContainer, CircleShape),
            )
        }
        if (more > 0) {
            Text(
                text = "+$more",
                style = MaterialTheme.typography.labelSmall,
                color = CinemerickTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 7.dp),
            )
        }
    }
}

// Previews

@Composable
private fun PreviewBar(
    expanded: Boolean = false,
    emphasized: Boolean = false,
    busy: Boolean = false,
    subtitle: String = "20:00–24:00 · 3D hidden · “Dune, Wicked”",
) {
    CinemerickSummaryBar(
        title = AnnotatedString(if (expanded) "Active filters" else "2 venues · Thu 13, Fri 14"),
        subtitle = if (expanded) "Tap a filter to go back to it" else subtitle,
        expanded = expanded,
        onToggle = {},
        description = "Filters",
        toggleLabel = "Show all filters",
        emphasizedSubtitle = emphasized,
        busy = busy,
        leading = if (expanded) null else ({ CinemerickSummaryDots(listOf(CinemaBrandColors.notoriousSilver, CinemaBrandColors.theSpaceOrange), more = 0) }),
        trailing = { CinemerickIconButton(icon = CinemerickIcons.Sliders, contentDescription = "Settings", onClick = {}, container = Color.Transparent, tint = CinemerickTheme.colors.onSurfaceVariant) },
    ) {
        CinemerickSummaryGroupLabel("Cinemas")
        CinemerickSummaryRow(AnnotatedString("Notorious"), onClick = {}, onClickLabel = "Go", subtitle = "Fiume Veneto") {
            CinemerickSummaryDot(CinemaBrandColors.notoriousSilver)
        }
        CinemerickSummaryRow(AnnotatedString("UCI"), onClick = {}, onClickLabel = "Go", subtitle = "Luxe Marcon, Pradamano, Villesse") {
            CinemerickSummaryDot(CinemaBrandColors.uciBlue)
        }
        CinemerickSummaryGroupLabel("Days and times")
        CinemerickSummaryRow(AnnotatedString("Thu 13 · 20:00–24:00"), onClick = {}, onClickLabel = "Go") {
            CinemerickSummaryRowIcon(CinemerickIcons.Calendar)
        }
        CinemerickSummaryGroupLabel("Format and film")
        CinemerickSummaryRow(AnnotatedString("“Dune, Wicked”"), onClick = {}, onClickLabel = "Go", flag = "Changed") {
            CinemerickSummaryRowIcon(CinemerickIcons.Search)
        }
        CinemerickSummaryRow(AnnotatedString("Pick at least one venue"), onClick = {}, onClickLabel = "Go", emphasized = true) {
            CinemerickSummaryRowIcon(CinemerickIcons.Plus)
        }
    }
}

@Preview(name = "Closed")
@Composable
private fun SummaryBarClosedPreview() {
    LightThemePreview { PreviewBar() }
}

@Preview(name = "Closed, dark")
@Composable
private fun SummaryBarClosedDarkPreview() {
    DarkThemePreview { PreviewBar() }
}

@Preview(name = "Open", heightDp = 640)
@Composable
private fun SummaryBarOpenPreview() {
    LightThemePreview { PreviewBar(expanded = true) }
}

@Preview(name = "Open, dark", heightDp = 640)
@Composable
private fun SummaryBarOpenDarkPreview() {
    DarkThemePreview { PreviewBar(expanded = true) }
}

@Preview(name = "Filters changed")
@Composable
private fun SummaryBarChangedPreview() {
    LightThemePreview { PreviewBar(emphasized = true, subtitle = "Filters changed") }
}

@Preview(name = "Searching")
@Composable
private fun SummaryBarSearchingPreview() {
    LightThemePreview { PreviewBar(busy = true, subtitle = "Searching…") }
}

@Preview(name = "Narrow", widthDp = 320)
@Composable
private fun SummaryBarNarrowPreview() {
    LightThemePreview { PreviewBar() }
}

@Preview(name = "Font scale 2x")
@Composable
private fun SummaryBarLargeFontPreview() {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density = density.density, fontScale = 2f)) {
        LightThemePreview { PreviewBar() }
    }
}
