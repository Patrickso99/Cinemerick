package com.preichert.cinemerick.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TOTAL_MS = 1500L
private const val STATIC_HOLD_MS = 400L

/**
 * Launch animation for all platforms. The ticket mark tilts in, the play button pops, the three stripes
 * grow in turn and the wordmark rises: about 1.5 s. With "Smooth animations" off, the final frame is shown
 * for 400 ms. Background and wordmark use the native launch colors (primaryContainer), so there is no flash.
 */
@Composable
fun CinemerickSplash(appName: String, onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalCinemerickColors.current
    val animationsEnabled = LocalAnimationsEnabled.current
    val currentOnFinished by rememberUpdatedState(onFinished)

    val ticket = remember { Animatable(0f) }
    val play = remember { Animatable(0f) }
    val stripes = remember { Animatable(0f) }
    val wordmark = remember { Animatable(0f) }

    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) {
            ticket.snapTo(1f)
            play.snapTo(1f)
            stripes.snapTo(1f)
            wordmark.snapTo(1f)
            delay(STATIC_HOLD_MS)
            currentOnFinished()
            return@LaunchedEffect
        }
        launch { ticket.animateTo(1f, tween(520, easing = Motion.Decelerate)) }
        launch {
            delay(260)
            play.animateTo(1f, tween(380, easing = Motion.Emphasized))
        }
        launch {
            delay(480)
            stripes.animateTo(1f, tween(560, easing = Motion.Decelerate))
        }
        launch {
            delay(860)
            wordmark.animateTo(1f, tween(360, easing = Motion.Decelerate))
        }
        delay(TOTAL_MS)
        currentOnFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(
                modifier = Modifier
                    .size(MarkSize)
                    .semantics { contentDescription = appName },
            ) {
                // Read in the draw phase, so the animation does not recompose.
                drawTicketMark(ticket.value, play.value, stripes.value)
            }
        }
    }
}

private val MarkSize = 260.dp

// The mark is drawn on a 1000 x 1000 grid, then scaled to the canvas.
private const val UNIT = 1000f
private val PlayColor = Color(0xFF4B2E83)
private val DashColor = Color(0xFF9C8CC8)
private val ItalyGreen = Color(0xFF009246)
private val ItalyRed = Color(0xFFCE2B37)

private data class Stripe(val x: Float, val color: Color, val outlined: Boolean)

private val StripeSpecs = listOf(
    Stripe(640f, ItalyGreen, outlined = false),
    Stripe(690f, Color.White, outlined = true),
    Stripe(740f, ItalyRed, outlined = false),
)

/** Ticket outline with a notch at the top and bottom, matching the app icon. */
private val TicketPath: Path by lazy {
    val left = 160f
    val top = 300f
    val right = 840f
    val bottom = 700f
    val r = 50f
    val notchX = 600f
    val nr = 42f
    Path().apply {
        moveTo(left + r, top)
        lineTo(notchX - nr, top)
        arcTo(Rect(notchX - nr, top - nr, notchX + nr, top + nr), 180f, -180f, false)
        lineTo(right - r, top)
        arcTo(Rect(right - 2 * r, top, right, top + 2 * r), 270f, 90f, false)
        lineTo(right, bottom - r)
        arcTo(Rect(right - 2 * r, bottom - 2 * r, right, bottom), 0f, 90f, false)
        lineTo(notchX + nr, bottom)
        arcTo(Rect(notchX - nr, bottom - nr, notchX + nr, bottom + nr), 0f, -180f, false)
        lineTo(left + r, bottom)
        arcTo(Rect(left, bottom - 2 * r, left + 2 * r, bottom), 90f, 90f, false)
        lineTo(left, top + r)
        arcTo(Rect(left, top, left + 2 * r, top + 2 * r), 180f, 90f, false)
        close()
    }
}

private val PlayPath: Path by lazy {
    Path().apply {
        moveTo(300f, 430f)
        lineTo(300f, 590f)
        lineTo(470f, 510f)
        close()
    }
}

private fun DrawScope.drawTicketMark(ticket: Float, play: Float, stripes: Float) {
    val pivot = Offset(500f, 500f)
    scale(size.minDimension / UNIT, Offset.Zero) {
        withTransform({
            // Tilts from -28 to -11 degrees (the icon's angle) and grows from 80% to 100%.
            rotate(-28f + 17f * ticket, pivot = pivot)
            scale(0.8f + 0.2f * ticket, 0.8f + 0.2f * ticket, pivot = pivot)
        }) {
            withTransform({ translate(top = 16f) }) {
                drawPath(TicketPath, color = Color.Black, alpha = 0.22f * ticket)
            }
            drawPath(TicketPath, color = Color.White, alpha = ticket)

            // Perforation under the notch.
            listOf(380f, 440f, 500f, 560f, 620f).forEach { y ->
                drawRoundRect(
                    color = DashColor,
                    topLeft = Offset(595f, y),
                    size = Size(10f, 30f),
                    cornerRadius = CornerRadius(5f),
                    alpha = ticket,
                )
            }

            // Play button pops in from its centre.
            withTransform({ scale(play, play, pivot = Offset(370f, 510f)) }) {
                drawPath(PlayPath, color = PlayColor, alpha = play)
            }

            // Stripes grow from the centre line, one after the other.
            StripeSpecs.forEachIndexed { i, stripe ->
                val k = ((stripes - i * 0.15f) / 0.85f).coerceIn(0f, 1f)
                val h = 260f * k
                if (h > 0f) {
                    val topLeft = Offset(stripe.x - 13f, 500f - h / 2f)
                    val radius = CornerRadius(minOf(13f, h / 2f))
                    drawRoundRect(
                        color = stripe.color,
                        topLeft = topLeft,
                        size = Size(26f, h),
                        cornerRadius = radius,
                        alpha = ticket,
                    )
                    if (stripe.outlined) {
                        drawRoundRect(
                            color = DashColor,
                            topLeft = topLeft,
                            size = Size(26f, h),
                            cornerRadius = radius,
                            style = Stroke(width = 4f),
                            alpha = ticket,
                        )
                    }
                }
            }
        }
    }
}
