package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun UciProgrammingDto.toShowings(venue: Venue, day: LocalDate): List<Showing> {
    val dayText = day.toString()
    return data.filter { it.title.isNotBlank() }.flatMap { movie ->
        val details = movie.toDetails()
        movie.screens
            .flatMap { variantsByFormat -> variantsByFormat.entries }
            .flatMap { (screen, variants) -> variants.map { variant -> variant.toFormat(screen) to variant } }
            .flatMap { (format, variant) ->
                variant.performances
                    .filter { it.day == dayText && it.actualStartAt.isNotBlank() }
                    .map { Showing(cleanTitle(movie.title), day, LocalTime.parse(it.actualStartAt), venue, format, movie.poster?.takeIf { it.isNotBlank() }, it.cartLink.toTicketUrl(UCI_BASE_URL), details = details) }
            }
    }
}

fun TheSpaceFilmsDto.toShowings(venue: Venue, day: LocalDate): List<Showing> {
    val dayText = day.toString()
    return result.filter { it.filmTitle.isNotBlank() }.flatMap { film ->
        val details = film.toDetails()
        film.showingGroups
            .flatMap { group -> group.sessions }
            .filter { it.startTime.take(10) == dayText && it.startTime.length >= 16 }
            .map {
                Showing(
                    cleanTitle(film.filmTitle), day, LocalTime.parse(it.startTime.substring(11, 16)), venue,
                    it.toFormat(), film.posterImageSrc?.takeIf { poster -> poster.isNotBlank() },
                    it.bookingUrl.toTicketUrl(THE_SPACE_BASE_URL),
                    soldOut = it.isSoldOut || !it.isBookingAvailable,
                    details = details
                )
            }
    }
}

// UCI publishes no director or cast; the trailer link is often missing.
private fun UciMovieDto.toDetails(): FilmDetails? = FilmDetails(
    synopsis = description.htmlToPlainText(),
    genre = genres.map { it.name }.joinedOrNull(),
    rating = rating?.name.orNullIfBlank(),
    trailerUrl = trailerLink.orNullIfBlank()
).takeUnless { it.isEmpty }

// The Space's genre list is empty and its trailer list has always been empty, so neither is read.
private fun TheSpaceFilmDto.toDetails(): FilmDetails? = FilmDetails(
    synopsis = synopsisShort.orNullIfBlank(),
    director = director.orNullIfBlank(),
    cast = cast.orNullIfBlank(),
    rating = toRating()
).takeUnless { it.isEmpty }

// e.g. "14+ · Violenza, Armi"; the chain's "t" means suitable for everyone.
private fun TheSpaceFilmDto.toRating(): String? {
    val age = certificate?.name.orNullIfBlank()?.let { if (it.equals("t", ignoreCase = true)) "Per tutti" else "$it+" }
    val reasons = secondaryCertificates.mapNotNull { it.name }.joinedOrNull()
    return listOfNotNull(age, reasons).joinedOrNull(TAG_SEPARATOR)
}

private const val TAG_SEPARATOR = " · "
private const val UCI_BASE_URL = "https://ucicinemas.it"
private const val THE_SPACE_BASE_URL = "https://www.thespacecinema.it"

// The sources hand out a path on their own site; a blank one means no purchase page.
private fun String?.toTicketUrl(baseUrl: String): String? = this?.takeIf { it.isNotBlank() }?.let { baseUrl + it }

// e.g. "XL", "2D", "2D · ENG · sub ITA"
private fun UciVariantDto.toFormat(screen: String): String? =
    listOfNotNull(
        screen.takeIf { it.isNotBlank() },
        language?.name?.takeIf { it.isNotBlank() && !it.equals("ITA", ignoreCase = true) },
        subtitles?.name?.takeIf { it.isNotBlank() }?.let { "sub $it" }
    ).joinToString(" · ").ifEmpty { null }

// e.g. "2D", "2D · EPIC", "2D · INFINITY VISION · VO"
private fun TheSpaceSessionDto.toFormat(): String? {
    val names = attributes.filter { it.name.isNotBlank() }
    return listOfNotNull(
        names.firstOrNull { it.attributeType == "Session" && it.name in DIMENSIONS }?.name,
        names.firstOrNull { it.name in EXPERIENCES }?.name,
        "VO".takeIf { names.any { it.attributeType == "Language" && it.name == ORIGINAL_LANGUAGE } }
    ).joinToString(" · ").ifEmpty { null }
}

private val DIMENSIONS = setOf("2D", "3D")
private val EXPERIENCES = setOf("EPIC", "INFINITY VISION")
private const val ORIGINAL_LANGUAGE = "LINGUA ORIGINALE"
