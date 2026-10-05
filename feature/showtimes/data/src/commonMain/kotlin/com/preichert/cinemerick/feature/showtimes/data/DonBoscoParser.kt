package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

private const val FILM_MARKER = "<article id=\"film-"

private val TITLE = Regex("alt=\"Poster ([^\"]+)\"")
private val POSTER = Regex("<img\\s+src=\"([^\"]+)\"\\s+alt=\"Poster ")

// Either a day heading ("LUNEDÌ 5 OTTOBRE", no year) or a time ("16.30"), read in page order: times belong to the heading before them.
private val DAY_OR_TIME = Regex(
    "class=\"giorno[^\"]*\">\\s*([^<]+?)\\s*</div>|class=\"cinema-promo-time\">\\s*(\\d{1,2})[.:](\\d{2})"
)
private val DAY_HEADING = Regex("(\\d{1,2})\\s+([A-Za-zÀ-ÿ]+)")

private val MONTHS = listOf(
    "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
    "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre"
)

// The home page lists every film with its days; the headings carry no year, so they are matched against the requested days.
internal fun parseDonBoscoShowings(html: String, venue: Venue, days: List<LocalDate>): List<Showing> =
    html.split(FILM_MARKER).drop(1).flatMap { parseFilm(it, venue, days) }.distinct()

private fun parseFilm(film: String, venue: Venue, days: List<LocalDate>): List<Showing> {
    val rawTitle = TITLE.find(film)?.groupValues?.get(1)?.unescapeHtml() ?: return emptyList()
    val title = cleanTitle(rawTitle)
    if (title.isBlank()) return emptyList()
    val poster = POSTER.find(film)?.groupValues?.get(1)?.unescapeHtml()

    var currentDay: LocalDate? = null
    val showings = mutableListOf<Showing>()
    for (match in DAY_OR_TIME.findAll(film)) {
        val heading = match.groups[1]?.value
        if (heading != null) {
            currentDay = days.firstOrNull { it.matches(heading) }
        } else {
            val day = currentDay ?: continue
            showings += Showing(
                title, day, LocalTime(match.groupValues[2].toInt(), match.groupValues[3].toInt()), venue, posterUrl = poster
            )
        }
    }
    return showings
}

private fun LocalDate.matches(heading: String): Boolean {
    val (dayOfMonth, monthName) = DAY_HEADING.find(heading)?.destructured ?: return false
    return dayOfMonth.toInt() == day && MONTHS.indexOf(monthName.lowercase()) + 1 == month.number
}
