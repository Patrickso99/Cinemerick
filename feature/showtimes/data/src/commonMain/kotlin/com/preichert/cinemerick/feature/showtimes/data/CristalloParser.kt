package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json

private const val FILM_MARKER = "<div class=\"amy-movie-item\">"
private const val SHOWTIMES_MARKER = "amy-movie-item-showtimes"

private val TITLE = Regex("class=\"amy-movie-field-title\">\\s*<a[^>]*>([\\s\\S]*?)</a>")
private val POSTER = Regex("<img[^>]*src=\"([^\"]+)\"")

// Times read "19.30", sometimes with a leading space or a note ("12.30 INGRESSO 5€"): only the start of the span matters.
private val TIME = Regex("<span>\\s*(\\d{1,2})[.:](\\d{2})")

// The day-switch call (`admin-ajax.php`) answers with a JSON string holding the HTML of that day's films.
internal fun parseCristalloShowings(response: String, venue: Venue, day: LocalDate): List<Showing> {
    val html = runCatching { Json.decodeFromString<String>(response) }.getOrDefault(response)
    return html.split(FILM_MARKER).drop(1).flatMap { parseFilm(it, venue, day) }.distinct()
}

private fun parseFilm(film: String, venue: Venue, day: LocalDate): List<Showing> {
    val rawTitle = TITLE.find(film)?.groupValues?.get(1)?.unescapeHtml() ?: return emptyList()
    val title = cleanTitle(rawTitle)
    if (title.isBlank()) return emptyList()
    val poster = POSTER.find(film)?.groupValues?.get(1)?.unescapeHtml()
    val showtimes = film.substringAfter(SHOWTIMES_MARKER, missingDelimiterValue = "")
    return TIME.findAll(showtimes).map {
        val (hour, minute) = it.destructured
        Showing(title, day, LocalTime(hour.toInt(), minute.toInt()), venue, posterUrl = poster)
    }.toList()
}
