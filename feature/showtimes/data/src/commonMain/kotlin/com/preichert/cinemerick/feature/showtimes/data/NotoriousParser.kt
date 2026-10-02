package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

private const val SITE_ROOT = "https://www.notoriouscinemas.it/"

private val FILM_CONTAINER = Regex("<div class=\"filmContainer[^\"]*\">")
private val TITLE = Regex("<div class=\"titolo\">([\\s\\S]*?)</div>")
private val POSTER = Regex("<img src=\"([^\"]*img_switcher[^\"]*)\"")
private val DAY = Regex("<div class=\"dayName\">[^<]*?(\\d{2})/(\\d{2})</div>([\\s\\S]*?)</li>")
private val TIME = Regex("class=\"orario_\"[^>]*>\\s*(\\d{1,2}:\\d{2})\\s*<")
private val TAG = Regex("<[^>]*>")
private val YEAR_TAG = Regex("\\s*\\[\\d{4}]")
private val NEW_BADGE = Regex("^NEW!\\s*")

// e.g. "DIGGER [2026] | ORIGINAL VERSION" -> title "Digger", format "VO"
private const val ORIGINAL_VERSION = "ORIGINAL VERSION"

// The page lists a whole week: per film its poster, then one `li.hours` per day (e.g. "Giovedì 01/10", no year) with the times.
internal fun parseNotoriousShowings(html: String, days: List<LocalDate>): List<Showing> {
    val daysByMonthDay = days.associateBy { it.month.number to it.day }
    val starts = FILM_CONTAINER.findAll(html).map { it.range.first }.toList()
    return starts.mapIndexed { index, start ->
        parseFilm(html.substring(start, starts.getOrNull(index + 1) ?: html.length), daysByMonthDay)
    }.flatten().distinct()
}

private fun parseFilm(film: String, daysByMonthDay: Map<Pair<Int, Int>, LocalDate>): List<Showing> {
    val rawTitle = TITLE.find(film)?.groupValues?.get(1)?.replace(TAG, "")?.unescapeHtml()?.trim() ?: return emptyList()
    val (name, suffix) = rawTitle.replace(NEW_BADGE, "").split("|", limit = 2).map { it.trim() }.let { it[0] to it.getOrNull(1) }
    val title = cleanTitle(name.replace(YEAR_TAG, ""))
    if (title.isBlank()) return emptyList()
    val format = suffix?.takeIf { it.isNotBlank() }?.let { if (it.equals(ORIGINAL_VERSION, ignoreCase = true)) "VO" else cleanTitle(it) }
    val poster = POSTER.find(film)?.groupValues?.get(1)?.replace("&amp;", "&")?.replace("../", SITE_ROOT)
    return DAY.findAll(film).flatMap { dayMatch ->
        val (dd, mm, content) = dayMatch.destructured
        val day = daysByMonthDay[mm.toInt() to dd.toInt()] ?: return@flatMap emptySequence()
        TIME.findAll(content).map {
            Showing(title, day, LocalTime.parse(it.groupValues[1].padStart(5, '0')), Cinema.NOTORIOUS, format, poster)
        }
    }.toList()
}
