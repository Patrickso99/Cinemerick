package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

internal const val ARCADIA_BASE_URL = "https://www.arcadiacinema.com"

private const val FILM_MARKER = "<div class=\"filmContainer\""

private val TITLE = Regex("<h1>\\s*([^<]+?)\\s*</h1>")
private val POSTER = Regex("<div class=\"locandina\">\\s*<img\\s+src=\"([^\"]+)\"")

// Either a day heading ("Martedì 15/12", no year) or a time link (purchase path, time) with its optional format ("INFINITY VISION"), read in page order.
private val FILM_PAGE = Regex("<a href=\"(/scheda/[^\"]+)\"")
private val DAY_OR_TIME = Regex(
    "class=\"dayName\">\\s*([^<]+?)\\s*</div>" +
        "|<a href=\"(/acquista/[^\"]*)\"[^>]*>\\s*(\\d{1,2}):(\\d{2})\\s*</a>(?:\\s*<div class=\"versione\">\\s*([^<]*?)\\s*</div>)?"
)
private val DAY_MONTH = Regex("(\\d{1,2})/(\\d{1,2})")

private val VENUE_LINK = Regex("<a href=\"/([a-z0-9-]+)\" title=\"([^\"]+)\"")

// The home page's menu links every cinema as <a href="/melzo" title="Melzo">, twice (header and footer).
internal fun parseArcadiaVenues(html: String): List<Venue> =
    VENUE_LINK.findAll(html)
        .map { it.groupValues[1] to it.groupValues[2].unescapeHtml() }
        .distinct()
        .map { (slug, name) -> Venue(Chain.ARCADIA, slug, name, "Lombardia", "$ARCADIA_BASE_URL/$slug") }
        .toList()

// A cinema page lists every film with all its days; the headings carry no year, so they are matched against the requested days.
internal fun parseArcadiaShowings(html: String, venue: Venue, days: List<LocalDate>): List<Showing> =
    html.split(FILM_MARKER).drop(1).flatMap { parseFilm(it, venue, days) }.distinct()

private fun parseFilm(film: String, venue: Venue, days: List<LocalDate>): List<Showing> {
    val title = cleanTitle(TITLE.find(film)?.groupValues?.get(1)?.unescapeHtml() ?: return emptyList())
    if (title.isBlank()) return emptyList()
    val poster = POSTER.find(film)?.groupValues?.get(1)?.unescapeHtml()?.toAbsoluteUrl()
    val detailsUrl = FILM_PAGE.find(film)?.groupValues?.get(1)?.unescapeHtml()?.toAbsoluteUrl()

    var currentDay: LocalDate? = null
    val showings = mutableListOf<Showing>()
    for (match in DAY_OR_TIME.findAll(film)) {
        val heading = match.groups[1]?.value
        if (heading != null) {
            currentDay = days.firstOrNull { it.matches(heading) }
        } else {
            val day = currentDay ?: continue
            showings += Showing(
                title, day, LocalTime(match.groupValues[3].toInt(), match.groupValues[4].toInt()), venue,
                format = match.groupValues[5].unescapeHtml().ifBlank { null }, posterUrl = poster,
                ticketUrl = ARCADIA_BASE_URL + match.groupValues[2].unescapeHtml(), detailsUrl = detailsUrl
            )
        }
    }
    return showings
}

private fun String.toAbsoluteUrl() = if (startsWith("/")) ARCADIA_BASE_URL + this else this

private fun LocalDate.matches(heading: String): Boolean {
    val (dayOfMonth, monthNumber) = DAY_MONTH.find(heading)?.destructured ?: return false
    return dayOfMonth.toInt() == day && monthNumber.toInt() == month.number
}
