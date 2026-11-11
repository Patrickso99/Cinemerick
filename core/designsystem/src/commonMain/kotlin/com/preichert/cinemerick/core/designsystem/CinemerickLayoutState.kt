package com.preichert.cinemerick.core.designsystem

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.foundation.focusable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Index of the filters item in the phone list: the large header is item 0. */
private const val FiltersItemIndex = 1

/** How long the ring stays on the component reached by a jump, before it fades. */
private const val HighlightHoldMillis = 1500L
private const val HighlightFadeMillis = 300

/**
 * Scroll state of [CinemerickAdaptiveLayout], shared with the screen so that it can jump to a component:
 * mark the components with [cinemerickJumpTarget], the bar that covers the top of the list with
 * [cinemerickScrollObstruction], then call [jumpTo].
 */
@Stable
class CinemerickLayoutState internal constructor(listIndex: Int, listOffset: Int, paneScroll: Int) {
    internal val listState = LazyListState(listIndex, listOffset)
    internal val paneScrollState = ScrollState(paneScroll)

    /** The scrollable that holds the filters in the current configuration, set by the layout. */
    internal var activeScrollable: ScrollableState? = null
    internal var viewport: LayoutCoordinates? = null
    internal var obstruction: LayoutCoordinates? = null
    internal val targets = mutableMapOf<Any, LayoutCoordinates>()

    internal var highlightedKey: Any? by mutableStateOf(null)
    internal val highlight = Animatable(0f)

    /**
     * The component that receives focus after a jump, so that TalkBack and VoiceOver announce where the jump landed.
     * Only that component is focusable (see [cinemerickJumpFocus]): otherwise every target would be an extra stop.
     */
    var focusedTargetKey: Any? by mutableStateOf(null)
        private set
    internal val focusRequester = FocusRequester()
    internal var focusHeld = false

    /**
     * Scrolls until the component marked with [key] sits just under the bar, then rings it for a moment.
     * Returns false when no such component is on the current layout (nothing happens then).
     */
    suspend fun jumpTo(key: Any, animate: Boolean, gapPx: Float): Boolean {
        val scrollable = activeScrollable ?: return false
        var target = targets[key]?.takeIf { it.isAttached }
        if (target == null && scrollable === listState) {
            // The filters item is not composed when the list is deep in the results: bring it back first.
            listState.scrollToItem(FiltersItemIndex)
            withFrameNanos { }
            target = targets[key]?.takeIf { it.isAttached }
        }
        val viewportTop = viewport?.takeIf { it.isAttached }?.positionInRoot()?.y ?: return false
        if (target == null) return false
        val barBottom = obstruction?.takeIf { it.isAttached }?.boundsInRoot()?.bottom ?: viewportTop
        val delta = target.positionInRoot().y - (maxOf(viewportTop, barBottom) + gapPx)
        if (animate) scrollable.animateScrollBy(delta) else scrollable.scrollBy(delta)
        moveFocusTo(key)
        flash(key, animate)
        return true
    }

    // The target becomes focusable on the next composition, then takes the focus.
    private suspend fun moveFocusTo(key: Any) {
        focusHeld = false
        focusedTargetKey = key
        withFrameNanos { }
        withFrameNanos { }
        runCatching { focusRequester.requestFocus() }
    }

    internal fun onTargetFocusChanged(focused: Boolean) {
        if (focused) {
            focusHeld = true
        } else if (focusHeld) {
            // Focus moved on: the target stops being focusable, so it is not an extra stop afterwards.
            focusHeld = false
            focusedTargetKey = null
        }
    }

    private suspend fun flash(key: Any, animate: Boolean) {
        highlightedKey = key
        highlight.snapTo(1f)
        delay(HighlightHoldMillis)
        if (animate) highlight.animateTo(0f, tween(HighlightFadeMillis)) else highlight.snapTo(0f)
        if (highlightedKey == key) highlightedKey = null
    }

    internal companion object {
        val Saver = listSaver<CinemerickLayoutState, Int>(
            save = { listOf(it.listState.firstVisibleItemIndex, it.listState.firstVisibleItemScrollOffset, it.paneScrollState.value) },
            restore = { CinemerickLayoutState(it[0], it[1], it[2]) },
        )
    }
}

/** A [CinemerickLayoutState] that survives configuration changes and process death. */
@Composable
fun rememberCinemerickLayoutState(): CinemerickLayoutState =
    rememberSaveable(saver = CinemerickLayoutState.Saver) { CinemerickLayoutState(0, 0, 0) }

/**
 * Marks a component that [CinemerickLayoutState.jumpTo] can reach with [key], and draws the ring on it after a jump:
 * a 2dp [color] stroke on [shape] plus a 4dp halo at 22%, read in the draw phase so it never recomposes.
 */
fun Modifier.cinemerickJumpTarget(state: CinemerickLayoutState, key: Any, shape: Shape, color: Color): Modifier =
    onPlaced { state.targets[key] = it }
        .drawWithContent {
            drawContent()
            val alpha = if (state.highlightedKey == key) state.highlight.value else 0f
            if (alpha <= 0f) return@drawWithContent
            val ring = 2.dp.toPx()
            val halo = 4.dp.toPx()
            val outer = ring + halo / 2
            translate(-outer, -outer) {
                val haloOutline = shape.createOutline(Size(size.width + outer * 2, size.height + outer * 2), layoutDirection, this)
                drawOutline(haloOutline, color.copy(alpha = 0.22f * alpha), style = Stroke(halo))
            }
            translate(-ring / 2, -ring / 2) {
                val ringOutline = shape.createOutline(Size(size.width + ring, size.height + ring), layoutDirection, this)
                drawOutline(ringOutline, color.copy(alpha = alpha), style = Stroke(ring))
            }
        }

/**
 * Makes the component reached by a jump focusable, with [label] as what a screen reader says, and gives it the focus.
 * Apply it only to the component whose key is [CinemerickLayoutState.focusedTargetKey]. It takes the focus itself rather
 * than a child, so a text field is reached without opening the keyboard.
 */
fun Modifier.cinemerickJumpFocus(state: CinemerickLayoutState, label: String): Modifier =
    focusRequester(state.focusRequester)
        .onFocusChanged { state.onTargetFocusChanged(it.isFocused) }
        .semantics { contentDescription = label }
        .focusable()

/** Marks the bar that covers the top of the scrolling content: a jump lands just under it. */
fun Modifier.cinemerickScrollObstruction(state: CinemerickLayoutState): Modifier =
    onPlaced { state.obstruction = it }
