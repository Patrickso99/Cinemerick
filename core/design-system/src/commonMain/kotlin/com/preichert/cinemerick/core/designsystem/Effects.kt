package com.preichert.cinemerick.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import kotlin.math.min

/**
 * Whether the smooth UI animations are enabled. Driven by the user preference in Settings.
 * When false every effect below degrades to a no-op / instant change.
 */
val LocalAnimationsEnabled = compositionLocalOf { true }

object Motion {
    const val Short = 200
    const val Medium = 350
    const val StaggerDelayMs = 50L
    const val MaxStaggerIndex = 8
    val Easing = FastOutSlowInEasing
}

/** Scales the content down while [interactionSource] is pressed. */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f,
): Modifier = composed {
    val enabled = LocalAnimationsEnabled.current
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
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

/** Fade + slide-up on first composition, delayed by [index] to produce a cascade. */
fun Modifier.staggeredAppear(index: Int): Modifier = composed {
    val enabled = LocalAnimationsEnabled.current
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(enabled) {
        if (enabled && progress.value < 1f) {
            delay(min(index, Motion.MaxStaggerIndex) * Motion.StaggerDelayMs)
            progress.animateTo(1f, tween(Motion.Medium, easing = Motion.Easing))
        } else {
            progress.snapTo(1f)
        }
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

/** Moves and fades the content slower than the surrounding scroll, e.g. for a header. */
fun Modifier.scrollParallax(scrollState: ScrollState): Modifier = composed {
    val enabled = LocalAnimationsEnabled.current
    if (!enabled) {
        Modifier
    } else {
        graphicsLayer {
            val offset = scrollState.value.toFloat()
            translationY = offset * 0.5f
            alpha = (1f - offset / (size.height * 1.5f).coerceAtLeast(1f)).coerceIn(0f, 1f)
        }
    }
}
