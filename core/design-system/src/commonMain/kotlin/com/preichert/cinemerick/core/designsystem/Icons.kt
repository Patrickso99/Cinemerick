package com.preichert.cinemerick.core.designsystem

import com.preichert.cinemerick.core.designsystem.resources.Res
import com.preichert.cinemerick.core.designsystem.resources.ic_add
import com.preichert.cinemerick.core.designsystem.resources.ic_alert
import com.preichert.cinemerick.core.designsystem.resources.ic_arrow_back
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
import org.jetbrains.compose.resources.DrawableResource

/**
 * Outline icon set of the redesign. Replaces the emoji glyphs (🎬 🍿 ⚠ ★ ‹) and the older filled icons.
 * Use with `painterResource(CinemerickIcons.Search)`; every icon-only control needs a contentDescription.
 */
object CinemerickIcons {
    val Search: DrawableResource get() = Res.drawable.ic_search
    val Sliders: DrawableResource get() = Res.drawable.ic_sliders
    val Cart: DrawableResource get() = Res.drawable.ic_cart
    val Copy: DrawableResource get() = Res.drawable.ic_copy
    val Play: DrawableResource get() = Res.drawable.ic_play
    val Star: DrawableResource get() = Res.drawable.ic_star
    val Calendar: DrawableResource get() = Res.drawable.ic_calendar
    val Check: DrawableResource get() = Res.drawable.ic_check
    val Plus: DrawableResource get() = Res.drawable.ic_add
    val Close: DrawableResource get() = Res.drawable.ic_close
    val ChevronRight: DrawableResource get() = Res.drawable.ic_chevron_right
    val ChevronUp: DrawableResource get() = Res.drawable.ic_chevron_up
    val ArrowBack: DrawableResource get() = Res.drawable.ic_arrow_back
    val Sun: DrawableResource get() = Res.drawable.ic_sun
    val Moon: DrawableResource get() = Res.drawable.ic_moon
    val Auto: DrawableResource get() = Res.drawable.ic_auto
    val Alert: DrawableResource get() = Res.drawable.ic_alert
    val Film: DrawableResource get() = Res.drawable.ic_film
}
