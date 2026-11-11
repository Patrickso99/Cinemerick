package com.preichert.cinemerick.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.preichert.cinemerick.core.designsystem.resources.Res
import com.preichert.cinemerick.core.designsystem.resources.plus_jakarta_sans_bold
import com.preichert.cinemerick.core.designsystem.resources.plus_jakarta_sans_extrabold
import com.preichert.cinemerick.core.designsystem.resources.plus_jakarta_sans_medium
import com.preichert.cinemerick.core.designsystem.resources.plus_jakarta_sans_regular
import com.preichert.cinemerick.core.designsystem.resources.plus_jakarta_sans_semibold
import org.jetbrains.compose.resources.Font

@Composable
private fun plusJakartaSans(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(Res.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

/**
 * Plus Jakarta Sans scale from docs/design (Main). Sizes and line heights are sp, so Italian labels wrap
 * instead of clipping when the system font scale grows.
 */
@Composable
fun cinemerickTypography(): Typography {
    val family = plusJakartaSans()
    return remember(family) {
        fun style(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = size.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = letterSpacing.sp,
        )
        Typography(
            displayLarge = style(FontWeight.ExtraBold, 40, 46, -0.8),
            displayMedium = style(FontWeight.ExtraBold, 36, 42, -0.8),
            displaySmall = style(FontWeight.ExtraBold, 32, 40, -0.6),
            headlineLarge = style(FontWeight.ExtraBold, 32, 40, -0.6),
            headlineMedium = style(FontWeight.ExtraBold, 28, 36, -0.5),
            headlineSmall = style(FontWeight.ExtraBold, 24, 32, -0.3),
            titleLarge = style(FontWeight.ExtraBold, 24, 32, -0.3),
            titleMedium = style(FontWeight.Bold, 18, 24),
            titleSmall = style(FontWeight.Bold, 15, 20),
            bodyLarge = style(FontWeight.Medium, 16, 24),
            bodyMedium = style(FontWeight.Medium, 14, 20),
            bodySmall = style(FontWeight.Medium, 13, 18),
            labelLarge = style(FontWeight.Bold, 14, 20),
            labelMedium = style(FontWeight.Bold, 12, 16, 0.4),
            labelSmall = style(FontWeight.Bold, 11, 16, 0.5),
        )
    }
}

/** The showtime time style: 16/20, weight 800, tabular numerals so the times line up in columns. */
@Composable
fun cinemerickTimeStyle(): TextStyle {
    val family = plusJakartaSans()
    return remember(family) {
        TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontFeatureSettings = "tnum",
        )
    }
}
