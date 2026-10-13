package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails

// Film pages (the "scheda" of a film) of the chains whose showtimes do not carry its details.

private val TAGS = Regex("<[^>]*>")

private fun String.plainText(): String? = replace(TAGS, "").unescapeHtml().replace(Regex("\\s+"), " ").trim().ifEmpty { null }

private fun String.field(regex: Regex): String? = regex.find(this)?.groupValues?.get(1)?.plainText()

// --- Notorious: `<div id="dati"><strong>Regia: </strong>Ben Gregor<br />...`, `<div id="trama">`, trailer button in the poster box.

private val NOTORIOUS_TRAMA = Regex("<div id=\"trama\">([\\s\\S]*?)</div>")
private val NOTORIOUS_TRAILER = Regex("<div class=\"box_trailer\"><a[^>]*href=\"([^\"]+)\"")
private val YOUTUBE_THUMBNAIL = Regex("img\\.youtube\\.com/vi/([\\w-]{11})/")

private fun notoriousLabel(label: String) = Regex("<strong>\\s*$label:\\s*</strong>\\s*([^<]*)")

internal fun parseNotoriousFilmPage(html: String): FilmDetails {
    // The button links a redirect page; the film's YouTube id is in the thumbnail next to it.
    val trailer = NOTORIOUS_TRAILER.find(html)?.groupValues?.get(1)?.unescapeHtml()?.let { redirect ->
        YOUTUBE_THUMBNAIL.find(html)?.groupValues?.get(1)?.let { id -> "https://www.youtube.com/watch?v=$id" } ?: redirect
    }
    return FilmDetails(
        synopsis = NOTORIOUS_TRAMA.find(html)?.groupValues?.get(1).htmlToPlainText(),
        genre = html.field(notoriousLabel("Genere")),
        director = html.field(notoriousLabel("Regia")),
        cast = html.field(notoriousLabel("Cast")),
        trailerUrl = trailer
    )
}

// --- Arcadia: `<div class="regia"><span class="label">Regia:</span> ...</div>`, `<div id="trama">`, trailers only inside the film's own box.

private val ARCADIA_TRAMA = Regex("<div id=\"trama\">([\\s\\S]*?)</div>")
private val YOUTUBE_LINK = Regex("https://(?:www\\.)?youtube\\.com/(?:watch\\?v=|embed/)[\\w-]+")

private fun arcadiaField(name: String) = Regex("<div class=\"$name\"><span class=\"label\">[^<]*</span>([^<]*)</div>")

internal fun parseArcadiaFilmPage(html: String): FilmDetails {
    // The page header carries a reel of other films' trailers: only the film's own section counts.
    val film = html.substringAfter("<div id=\"scheda\">", missingDelimiterValue = "").substringBefore("<div id=\"preno\">")
    return FilmDetails(
        synopsis = ARCADIA_TRAMA.find(html)?.groupValues?.get(1).htmlToPlainText(),
        genre = html.field(arcadiaField("genere")),
        director = html.field(arcadiaField("regia")),
        cast = html.field(arcadiaField("cast")),
        trailerUrl = YOUTUBE_LINK.find(film)?.value
    )
}

// --- Cristallo: WordPress movie page with `<li><label>Regia:</label><span>...</span></li>` and the plot after a "Trama" heading.

private val CRISTALLO_PLOT = Regex("<h3 class=\"info-name amy-title\">\\s*Trama\\s*</h3>([\\s\\S]*?)</div>")
private val PARAGRAPH = Regex("<p>([\\s\\S]*?)</p>")

private fun cristalloField(label: String) = Regex("<label>\\s*(?:$label):\\s*</label>\\s*<span>([\\s\\S]*?)</span>")

internal fun parseCristalloFilmPage(html: String): FilmDetails {
    // Credits (country, distributor...) follow the plot in paragraphs that start with a bold label.
    val plot = CRISTALLO_PLOT.find(html)?.groupValues?.get(1)?.let { block ->
        PARAGRAPH.findAll(block).map { it.value }.filterNot { "<strong>" in it }.joinToString("")
    }
    return FilmDetails(
        synopsis = plot.htmlToPlainText(),
        genre = html.field(cristalloField("Genere")),
        director = html.field(cristalloField("Regia")),
        cast = html.field(cristalloField("Cast|Attori|Con"))
    )
}

// --- 18Tickets (Cinergia, Cinemazero): `<p class='movie__option'><strong>Regia:</strong> ...</p>`, plot in the `plotPanel` accordion.

private val TICKETS_PLOT = Regex("id='plotPanel'>\\s*<div class='card-body'>([\\s\\S]*?)</div>")
private val TICKETS_TRAILER = Regex("<a class='trailer-sample[^']*' href='([^']+)'")
private val TICKETS_AGE = Regex("<strong>\\s*Età\\s*</strong>\\s*([^<]*)")

// Cinemazero has no "Età" line, only the classification icon: `content_classification_icons/0.png` ("Opere per tutti").
private val TICKETS_AGE_ICON = Regex("content_classification_icons/(\\d+)\\.png")

private fun ticketsOption(label: String) = Regex("<strong>\\s*$label:\\s*</strong>\\s*([^<]*)")

internal fun parseEighteenTicketsFilmPage(html: String): FilmDetails {
    val age = html.field(TICKETS_AGE) ?: TICKETS_AGE_ICON.find(html)?.groupValues?.get(1)?.let { if (it == "0") "Per tutti" else it }
    return FilmDetails(
        synopsis = TICKETS_PLOT.find(html)?.groupValues?.get(1).htmlToPlainText(),
        genre = html.field(ticketsOption("Genere")),
        director = html.field(ticketsOption("Regia")),
        cast = html.field(ticketsOption("Con")),
        rating = age?.let { if (it.all(Char::isDigit) && it != "0") "$it+" else it },
        trailerUrl = TICKETS_TRAILER.find(html)?.groupValues?.get(1)?.unescapeHtml()
    )
}
