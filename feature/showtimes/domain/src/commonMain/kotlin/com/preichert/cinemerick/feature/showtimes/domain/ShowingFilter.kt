package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

fun List<Showing>.filterShowings(
    ranges: List<DayRange>,
    filmQueries: List<String>,
    now: LocalDateTime,
    hiddenTags: Set<String> = emptySet()
): List<Showing> {
    val rangeByDay = ranges.associateBy { it.date }
    val nowTime = LocalTime(now.hour, now.minute)
    val queries = filmQueries.map { it.trim().lowercase() }.filter { it.isNotEmpty() }

    return filter { showing ->
        val range = rangeByDay[showing.day] ?: return@filter false
        val afterMin = range.min == null || showing.time >= range.min
        val beforeMax = range.max == null || showing.time <= range.max
        val notStarted = !(showing.day == now.date && showing.time <= nowTime)
        val matchesFilm = queries.isEmpty() || queries.any { it in showing.title.lowercase() }
        val visibleFormat = hiddenTags.isEmpty() || showing.tags.none { it in hiddenTags }
        afterMin && beforeMax && notStarted && matchesFilm && visibleFormat
    }.distinct()
}
