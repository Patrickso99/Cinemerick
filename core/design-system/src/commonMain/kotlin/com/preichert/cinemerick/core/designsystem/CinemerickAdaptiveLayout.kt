package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.presentation.util.DeviceConfiguration
import com.preichert.cinemerick.core.presentation.util.currentDeviceConfiguration

/** Scroll distance over which the large phone header collapses into the compact bar. */
private val CollapseDistance = 72.dp

/**
 * Responsive layout of the redesign. The configuration comes from the window size class:
 * - phone portrait: one scrolling column; the large header scrolls away and a compact bar pins at the top
 *   once it collapses; [actionBar] floats at the bottom.
 * - phone landscape and tablet portrait: filters and results side by side or stacked, no gradient header.
 * - tablet landscape and desktop: two panes (filters | results), desktop adds the navigation rail
 *   ([desktopRail]) on the left.
 *
 * @param header the large header (title, tagline). On wide layouts it is the top of the filter pane.
 * @param filtersContent the filter sections, in order
 * @param collapsedTitle title shown in the pinned compact bar on phones once [header] scrolls away
 * @param collapsedAction trailing action of the compact bar (e.g. the settings button)
 * @param actionBar sticky actions (Clear / Search). Floats at the bottom on phones, ends the filter pane on wide layouts
 * @param resultsContent results of a wide layout, shown in the right pane
 * @param mobileResultsItems results of the phone layout, as lazy items
 * @param desktopRail navigation rail, shown on desktop only
 */
@Composable
fun CinemerickAdaptiveLayout(
    header: @Composable () -> Unit,
    filtersContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    collapsedTitle: String? = null,
    collapsedAction: (@Composable () -> Unit)? = null,
    actionBar: (@Composable () -> Unit)? = null,
    resultsContent: (@Composable ColumnScope.() -> Unit)? = null,
    mobileResultsItems: (LazyListScope.() -> Unit)? = null,
    desktopRail: (@Composable () -> Unit)? = null,
) {
    val colors = CinemerickTheme.colors
    when (val configuration = currentDeviceConfiguration()) {
        DeviceConfiguration.MOBILE_PORTRAIT -> if (mobileResultsItems != null) {
            PhoneLazyLayout(
                modifier = modifier,
                header = header,
                filtersContent = filtersContent,
                collapsedTitle = collapsedTitle,
                collapsedAction = collapsedAction,
                actionBar = actionBar,
                mobileResultsItems = mobileResultsItems,
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                header()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(horizontal = CinemerickTheme.spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space4),
                    content = filtersContent,
                )
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // Phone landscape: filters on the left, results on the right
        DeviceConfiguration.MOBILE_LANDSCAPE -> if (resultsContent != null) {
            TwoPanes(
                modifier = modifier,
                header = header,
                filtersContent = filtersContent,
                actionBar = actionBar,
                resultsContent = resultsContent,
                filterWeight = 0.9f,
                resultsWeight = 1.1f,
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState()),
            ) {
                header()
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = CinemerickTheme.spacing.space4),
                    verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space3),
                    content = filtersContent,
                )
            }
        }

        // Tablet portrait: filters and results stacked, both in the content column
        // Tablet portrait: the same two panes as the desktop, filters on the left and results on the right
        DeviceConfiguration.TABLET_PORTRAIT -> if (resultsContent != null) {
            TwoPanes(
                modifier = modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                header = header,
                filtersContent = filtersContent,
                actionBar = actionBar,
                resultsContent = resultsContent,
                filterWidth = 340.dp,
            )
        } else Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.background)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = CinemerickTheme.spacing.space6),
                verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space4),
            ) {
                header()
                filtersContent()
                actionBar?.invoke()
                resultsContent?.invoke(this)
            }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        // Tablet landscape and desktop: two panes, desktop adds the navigation rail
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            val isDesktop = configuration == DeviceConfiguration.DESKTOP
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                if (isDesktop && desktopRail != null) {
                    desktopRail()
                }
                if (resultsContent != null) {
                    TwoPanes(
                        modifier = Modifier.weight(1f),
                        header = header,
                        filtersContent = filtersContent,
                        actionBar = actionBar,
                        resultsContent = resultsContent,
                        filterWidth = if (isDesktop) 340.dp else 360.dp,
                    )
                } else {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space4),
                        ) {
                            header()
                            filtersContent()
                        }
                    }
                }
            }
        }
    }
}

/** Filter pane (fixed width, hairline on its right edge) and results pane. */
@Composable
private fun TwoPanes(
    header: @Composable () -> Unit,
    filtersContent: @Composable ColumnScope.() -> Unit,
    resultsContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    actionBar: (@Composable () -> Unit)? = null,
    filterWidth: Dp? = null,
    filterWeight: Float = 1f,
    resultsWeight: Float = 1f,
) {
    val colors = CinemerickTheme.colors
    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = (if (filterWidth != null) Modifier.width(filterWidth) else Modifier.weight(filterWeight))
                .fillMaxHeight()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space4),
        ) {
            header()
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space4),
                content = filtersContent,
            )
            actionBar?.invoke()
        }
        Spacer(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(colors.outlineVariant),
        )
        Column(
            modifier = Modifier
                .weight(resultsWeight)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space4),
            content = resultsContent,
        )
    }
}

/** Phone layout: lazy list with the large header, filters and results, plus the pinned compact bar and action bar. */
@Composable
private fun PhoneLazyLayout(
    header: @Composable () -> Unit,
    filtersContent: @Composable ColumnScope.() -> Unit,
    collapsedTitle: String?,
    collapsedAction: (@Composable () -> Unit)?,
    actionBar: (@Composable () -> Unit)?,
    mobileResultsItems: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    val listState = rememberLazyListState()
    val collapseDistancePx = with(LocalDensity.current) { CollapseDistance.toPx() }
    val collapsedFraction by remember(listState, collapseDistancePx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / collapseDistancePx).coerceIn(0f, 1f)
            }
        }
    }
    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .imePadding(),
            contentPadding = PaddingValues(bottom = if (actionBar != null) 104.dp else 0.dp),
        ) {
            item(key = "header", contentType = "header") { header() }
            item(key = "filters", contentType = "filters") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(CinemerickTheme.spacing.space6),
                    content = filtersContent,
                )
            }
            mobileResultsItems()
            item(key = "footer", contentType = "footer") {
                Spacer(modifier = Modifier.height(96.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        // Pinned compact bar: fades in as the large header scrolls away
        if (collapsedTitle != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = collapsedFraction }
                    .background(colors.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(64.dp)
                    .padding(start = 20.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = collapsedTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                collapsedAction?.invoke()
            }
        }

        if (actionBar != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                actionBar()
            }
        }
    }
}
