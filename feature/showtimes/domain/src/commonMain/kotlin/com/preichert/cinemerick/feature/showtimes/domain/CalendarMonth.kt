package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.datetime.until

/**
 * Monday-first grid of a month: `null` pads the cells before the 1st, then one date per day.
 */
fun monthGrid(year: Int, month: Month): List<LocalDate?> {
    val first = LocalDate(year, month, 1)
    val length = first.until(first.plus(1, DateTimeUnit.MONTH), DateTimeUnit.DAY).toInt()
    val leading = first.dayOfWeek.ordinal
    return List(leading) { null } + List(length) { first.plus(it, DateTimeUnit.DAY) }
}

/**
 * The next weekend days from [today]: Saturday and Sunday, or only Sunday when today is Sunday.
 */
fun upcomingWeekend(today: LocalDate): List<LocalDate> {
    if (today.dayOfWeek == DayOfWeek.SUNDAY) return listOf(today)
    val daysToSaturday = (DayOfWeek.SATURDAY.ordinal - today.dayOfWeek.ordinal + 7) % 7
    val saturday = today.plus(daysToSaturday, DateTimeUnit.DAY)
    return listOf(saturday, saturday.plus(1, DateTimeUnit.DAY))
}

fun Month.italianName(): String = when (this) {
    Month.JANUARY -> "Gennaio"
    Month.FEBRUARY -> "Febbraio"
    Month.MARCH -> "Marzo"
    Month.APRIL -> "Aprile"
    Month.MAY -> "Maggio"
    Month.JUNE -> "Giugno"
    Month.JULY -> "Luglio"
    Month.AUGUST -> "Agosto"
    Month.SEPTEMBER -> "Settembre"
    Month.OCTOBER -> "Ottobre"
    Month.NOVEMBER -> "Novembre"
    Month.DECEMBER -> "Dicembre"
}
