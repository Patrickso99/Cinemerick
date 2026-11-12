package com.preichert.cinemerick.core.designsystem

import com.preichert.cinemerick.core.designsystem.resources.Res
import com.preichert.cinemerick.core.designsystem.resources.ic_add
import com.preichert.cinemerick.core.designsystem.resources.ic_assistant
import com.preichert.cinemerick.core.designsystem.resources.ic_mic
import com.preichert.cinemerick.core.designsystem.resources.ic_mic_off
import com.preichert.cinemerick.core.designsystem.resources.ic_keyboard
import com.preichert.cinemerick.core.designsystem.resources.ic_edit
import com.preichert.cinemerick.core.designsystem.resources.ic_key
import com.preichert.cinemerick.core.designsystem.resources.ic_cloud
import com.preichert.cinemerick.core.designsystem.resources.ic_retry
import com.preichert.cinemerick.core.designsystem.resources.ic_question
import com.preichert.cinemerick.core.designsystem.resources.ic_lock
import com.preichert.cinemerick.core.designsystem.resources.ic_eye
import com.preichert.cinemerick.core.designsystem.resources.ic_eye_off
import com.preichert.cinemerick.core.designsystem.resources.ic_alert
import com.preichert.cinemerick.core.designsystem.resources.ic_arrow_back
import com.preichert.cinemerick.core.designsystem.resources.ic_arrow_up_right
import com.preichert.cinemerick.core.designsystem.resources.ic_clock
import com.preichert.cinemerick.core.designsystem.resources.ic_auto
import com.preichert.cinemerick.core.designsystem.resources.ic_cart
import com.preichert.cinemerick.core.designsystem.resources.ic_calendar
import com.preichert.cinemerick.core.designsystem.resources.ic_check
import com.preichert.cinemerick.core.designsystem.resources.ic_chevron_right
import com.preichert.cinemerick.core.designsystem.resources.ic_chevron_up
import com.preichert.cinemerick.core.designsystem.resources.ic_close
import com.preichert.cinemerick.core.designsystem.resources.ic_copy
import com.preichert.cinemerick.core.designsystem.resources.ic_film
import com.preichert.cinemerick.core.designsystem.resources.ic_moon
import com.preichert.cinemerick.core.designsystem.resources.ic_play
import com.preichert.cinemerick.core.designsystem.resources.ic_search
import com.preichert.cinemerick.core.designsystem.resources.ic_sliders
import com.preichert.cinemerick.core.designsystem.resources.ic_star
import com.preichert.cinemerick.core.designsystem.resources.ic_sun
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource

/**
 * Outline icon set of the redesign. Replaces the emoji glyphs (🎬 🍿 ⚠ ★ ‹) and the older filled icons.
 * Use as `Icon(painter = CinemerickIcons.Search, …)`; every icon-only control needs a contentDescription.
 */
object CinemerickIcons {
    val Search: Painter @Composable get() = painterResource(Res.drawable.ic_search)
    val Sliders: Painter @Composable get() = painterResource(Res.drawable.ic_sliders)
    val Cart: Painter @Composable get() = painterResource(Res.drawable.ic_cart)
    val Copy: Painter @Composable get() = painterResource(Res.drawable.ic_copy)
    val Play: Painter @Composable get() = painterResource(Res.drawable.ic_play)
    val Star: Painter @Composable get() = painterResource(Res.drawable.ic_star)
    val Calendar: Painter @Composable get() = painterResource(Res.drawable.ic_calendar)
    val Check: Painter @Composable get() = painterResource(Res.drawable.ic_check)
    val Plus: Painter @Composable get() = painterResource(Res.drawable.ic_add)
    val Close: Painter @Composable get() = painterResource(Res.drawable.ic_close)
    val ChevronRight: Painter @Composable get() = painterResource(Res.drawable.ic_chevron_right)
    val ChevronUp: Painter @Composable get() = painterResource(Res.drawable.ic_chevron_up)
    val ArrowBack: Painter @Composable get() = painterResource(Res.drawable.ic_arrow_back)
    val Sun: Painter @Composable get() = painterResource(Res.drawable.ic_sun)
    val Moon: Painter @Composable get() = painterResource(Res.drawable.ic_moon)
    val Auto: Painter @Composable get() = painterResource(Res.drawable.ic_auto)
    val Alert: Painter @Composable get() = painterResource(Res.drawable.ic_alert)
    val Film: Painter @Composable get() = painterResource(Res.drawable.ic_film)
    val Clock: Painter @Composable get() = painterResource(Res.drawable.ic_clock)
    val ArrowUpRight: Painter @Composable get() = painterResource(Res.drawable.ic_arrow_up_right)

    // Voice assistant. Assistant is the microphone with the AI spark, for the assistant button only.
    val Assistant: Painter @Composable get() = painterResource(Res.drawable.ic_assistant)
    val Mic: Painter @Composable get() = painterResource(Res.drawable.ic_mic)
    val MicOff: Painter @Composable get() = painterResource(Res.drawable.ic_mic_off)
    val Keyboard: Painter @Composable get() = painterResource(Res.drawable.ic_keyboard)
    val Edit: Painter @Composable get() = painterResource(Res.drawable.ic_edit)
    val Key: Painter @Composable get() = painterResource(Res.drawable.ic_key)
    val Cloud: Painter @Composable get() = painterResource(Res.drawable.ic_cloud)
    val Retry: Painter @Composable get() = painterResource(Res.drawable.ic_retry)
    val Question: Painter @Composable get() = painterResource(Res.drawable.ic_question)
    val Lock: Painter @Composable get() = painterResource(Res.drawable.ic_lock)
    val Eye: Painter @Composable get() = painterResource(Res.drawable.ic_eye)
    val EyeOff: Painter @Composable get() = painterResource(Res.drawable.ic_eye_off)
}
