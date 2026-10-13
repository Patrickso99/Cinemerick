package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

private val SCHEDULE_MARKER = Regex("id='schedule-\\d+'")
private val TITLE = Regex("class='movie__title'[^>]*>\\s*([^<\\s][^<]*?)\\s*</a>")
private val POSTER = Regex("<img[^>]*src='([^']+)'")
private val SCHEDULE_DAY = Regex("(\\d{2})/(\\d{2})/(\\d{4})")
private val TIME = Regex("data-time='[^']*'([^>]*)>\\s*<li[^>]*>[\\s\\S]*?(\\d{1,2}:\\d{2})")
// e.g. https://coneglianocinergia.18tickets.it/film/91221?ref_date=2026-10-13 -> the page without the date.
private val FILM_PAGE = Regex("href='(https?://[^']*/film/\\d+)(?:\\?[^']*)?'")
private val HREF = Regex("href='([^']*)'")
private val ORIGINAL_LANGUAGE_PREFIX = Regex("^\\(\\s*V\\.?O\\.?S?\\.?\\s*\\)\\s*", RegexOption.IGNORE_CASE)
private val ORIGINAL_LANGUAGE_SUFFIX = Regex("\\s*-\\s*V\\.?\\s*O\\.?\\s*$", RegexOption.IGNORE_CASE)

// The site answers its day-switch call (`/film/fetch_films`) with a script that fills the page with an escaped HTML string.
private fun String.unescapeJs(): String =
    replace("\\'", "'").replace("\\\"", "\"").replace("\\n", "\n").replace("<\\/", "</").replace("\\\\", "\\")

// Each film is: title/poster block, then a `schedule-<id>` section holding the day and its time links.
internal fun parseEighteenTicketsShowings(response: String, venue: Venue, day: LocalDate): List<Showing> {
    val html = response.unescapeJs()
    val markers = SCHEDULE_MARKER.findAll(html).toList()
    return markers.mapIndexed { index, marker ->
        val infoStart = if (index == 0) 0 else markers[index - 1].range.last
        val info = html.substring(infoStart, marker.range.first)
        val schedule = html.substring(marker.range.last, markers.getOrNull(index + 1)?.range?.first ?: html.length)
        parseFilm(info, schedule, venue, day)
    }.flatten().distinct()
}

private fun parseFilm(info: String, schedule: String, venue: Venue, day: LocalDate): List<Showing> {
    val rawTitle = TITLE.findAll(info).lastOrNull()?.groupValues?.get(1)?.unescapeHtml() ?: return emptyList()
    if (!scheduleIsFor(schedule, day)) return emptyList()
    val isOriginalLanguage = ORIGINAL_LANGUAGE_PREFIX.containsMatchIn(rawTitle) || ORIGINAL_LANGUAGE_SUFFIX.containsMatchIn(rawTitle)
    val title = cleanTitle(rawTitle.replace(ORIGINAL_LANGUAGE_PREFIX, "").replace(ORIGINAL_LANGUAGE_SUFFIX, ""))
    val poster = POSTER.findAll(info).lastOrNull()?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
    // `info` also holds the end of the previous film's block, so like the title and poster the film's own link is the last one.
    val detailsUrl = FILM_PAGE.findAll(info).lastOrNull()?.groupValues?.get(1)
    return TIME.findAll(schedule).map {
        val ticketUrl = HREF.find(it.groupValues[1])?.groupValues?.get(1)?.takeIf { href -> href.startsWith("http") }
        Showing(title, day, LocalTime.parse(it.groupValues[2].padStart(5, '0')), venue, "VO".takeIf { isOriginalLanguage }, poster, ticketUrl, detailsUrl = detailsUrl)
    }.toList()
}

// The header reads e.g. "Giovedì 01/10/2026": ignore a page that shows another day.
private fun scheduleIsFor(schedule: String, day: LocalDate): Boolean {
    val (dd, mm, yyyy) = SCHEDULE_DAY.find(schedule)?.destructured ?: return true
    return "$yyyy-$mm-$dd" == day.toString()
}
