package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.runtime.Composable
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_mon_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_mon_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_mon_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_tue_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_tue_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_tue_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_wed_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_wed_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_wed_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_thu_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_thu_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_thu_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_fri_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_fri_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_fri_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_sat_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_sat_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_sat_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_sun_full
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_sun_initial
import com.preichert.cinemerick.feature.showtimes.presentation.resources.weekday_sun_short
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_jan
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_feb
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_mar
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_apr
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_may
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_jun
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_jul
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_aug
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_sep
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_oct
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_nov
import com.preichert.cinemerick.feature.showtimes.presentation.resources.month_dec
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Weekday and month names in the language of the app. The poll text is the exception and stays Italian (domain module).

internal fun DayOfWeek.fullNameResource(): StringResource = when (this) {
    DayOfWeek.MONDAY -> Res.string.weekday_mon_full
    DayOfWeek.TUESDAY -> Res.string.weekday_tue_full
    DayOfWeek.WEDNESDAY -> Res.string.weekday_wed_full
    DayOfWeek.THURSDAY -> Res.string.weekday_thu_full
    DayOfWeek.FRIDAY -> Res.string.weekday_fri_full
    DayOfWeek.SATURDAY -> Res.string.weekday_sat_full
    DayOfWeek.SUNDAY -> Res.string.weekday_sun_full
}

/** "Wed" / "Mer". */
internal fun DayOfWeek.shortNameResource(): StringResource = when (this) {
    DayOfWeek.MONDAY -> Res.string.weekday_mon_short
    DayOfWeek.TUESDAY -> Res.string.weekday_tue_short
    DayOfWeek.WEDNESDAY -> Res.string.weekday_wed_short
    DayOfWeek.THURSDAY -> Res.string.weekday_thu_short
    DayOfWeek.FRIDAY -> Res.string.weekday_fri_short
    DayOfWeek.SATURDAY -> Res.string.weekday_sat_short
    DayOfWeek.SUNDAY -> Res.string.weekday_sun_short
}

/** "W" / "M": the calendar header's initial, which differs between languages. */
internal fun DayOfWeek.initialResource(): StringResource = when (this) {
    DayOfWeek.MONDAY -> Res.string.weekday_mon_initial
    DayOfWeek.TUESDAY -> Res.string.weekday_tue_initial
    DayOfWeek.WEDNESDAY -> Res.string.weekday_wed_initial
    DayOfWeek.THURSDAY -> Res.string.weekday_thu_initial
    DayOfWeek.FRIDAY -> Res.string.weekday_fri_initial
    DayOfWeek.SATURDAY -> Res.string.weekday_sat_initial
    DayOfWeek.SUNDAY -> Res.string.weekday_sun_initial
}

internal fun Month.nameResource(): StringResource = when (this) {
    Month.JANUARY -> Res.string.month_jan
    Month.FEBRUARY -> Res.string.month_feb
    Month.MARCH -> Res.string.month_mar
    Month.APRIL -> Res.string.month_apr
    Month.MAY -> Res.string.month_may
    Month.JUNE -> Res.string.month_jun
    Month.JULY -> Res.string.month_jul
    Month.AUGUST -> Res.string.month_aug
    Month.SEPTEMBER -> Res.string.month_sep
    Month.OCTOBER -> Res.string.month_oct
    Month.NOVEMBER -> Res.string.month_nov
    Month.DECEMBER -> Res.string.month_dec
}

@Composable
internal fun DayOfWeek.fullName(): String = stringResource(fullNameResource())

@Composable
internal fun DayOfWeek.initial(): String = stringResource(initialResource())

@Composable
internal fun Month.displayName(): String = stringResource(nameResource())

/** "Wednesday 14/10" / "Mercoledì 14/10", for the per-day time window of the filters. */
@Composable
internal fun LocalDate.dayLabel(): String = "${stringResource(dayOfWeek.fullNameResource())} ${shortDate()}"

/** "WED" / "MER", for the day chips. */
@Composable
internal fun LocalDate.chipDayName(): String = stringResource(dayOfWeek.shortNameResource()).uppercase()
