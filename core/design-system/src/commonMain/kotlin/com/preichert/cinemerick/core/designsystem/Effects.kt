package com.preichert.cinemerick.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Whether the "Smooth animations" preference is on. When it is off every duration in [Motion] becomes 0,
 * there is no list stagger and no shared-element transition.
 */
val LocalAnimationsEnabled = compositionLocalOf { true }

/** Durations and curves from docs/design (Main, Motion). Use [Motion.duration] so the toggle is respected. */
object Motion {
    /** Press feedback: scale to 0.97. */
    const val Press = 90

    /** List items fade in and rise 12dp. */
    const val ListEnter = 220

    /** Delay between consecutive list items, applied to the first [StaggerMaxItems] only. */
    const val StaggerStepMs = 40L
    const val StaggerMaxItems = 8

    /** Poster to film details, shared bounds. */
    const val SharedBounds = 320

    /** Bottom sheets, decelerate. */
    const val Sheet = 300

    /** Decelerating curve for elements entering the screen. */
    val Decelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    /** Emphasized curve for shared bounds. */
    val Emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** [millis] when animations are on, 0 when the "Smooth animations" toggle is off. */
    fun duration(enabled: Boolean, millis: Int): Int = if (enabled) millis else 0
}

/** Scales the content to 0.97 while [interactionSource] is pressed. */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.97f,
): Modifier = composed {
    val enabled = LocalAnimationsEnabled.current
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) pressedScale else 1f,
        animationSpec = tween(Motion.duration(enabled, Motion.Press), easing = Motion.Decelerate),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** [clickable] with the default ripple plus a press-scale effect. */
fun Modifier.bounceClickable(
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    pressScale(interactionSource).clickable(
        interactionSource = interactionSource,
        indication = LocalIndication.current,
        enabled = enabled,
        role = role,
        onClick = onClick,
    )
}

/**
 * List entrance: fade in and rise 12dp over [Motion.ListEnter]. Items after the first [Motion.StaggerMaxItems]
 * appear together; with animations off the content is shown at once.
 */
fun Modifier.staggeredAppear(index: Int): Modifier = composed {
    val enabled = LocalAnimationsEnabled.current
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(enabled) {
        if (enabled && progress.value < 1f) {
            delay(minOf(index, Motion.StaggerMaxItems) * Motion.StaggerStepMs)
            progress.animateTo(1f, tween(Motion.ListEnter, easing = Motion.Decelerate))
        } else {
            progress.snapTo(1f)
        }
    }
    graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * 12.dp.toPx()
    }
}

/**
 * Placement animation of a lazy item, off when "Smooth animations" is off. Use it in place of `Modifier.animateItem()`.
 */
@Composable
fun LazyItemScope.animateItemIfEnabled(): Modifier =
    if (LocalAnimationsEnabled.current) Modifier.animateItem() else Modifier
