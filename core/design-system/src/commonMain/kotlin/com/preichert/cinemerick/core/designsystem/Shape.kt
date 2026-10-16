package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape scale of the redesign: xs 8 (format tag), sm 12 (chip, badge), md 16 (poster, input),
 * lg 20 (card), xl 28 (sheet, bar). [CinemerickFullShape] is the pill used by buttons and switches.
 */
val CinemerickShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val CinemerickFullShape = RoundedCornerShape(percent = 50)
