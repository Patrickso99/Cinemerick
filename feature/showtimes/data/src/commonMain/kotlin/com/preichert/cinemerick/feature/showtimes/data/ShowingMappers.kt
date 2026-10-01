package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.Showing
import com.preichert.cinemerick.feature.showtimes.domain.cleanTitle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun UciProgrammingDto.toShowings(day: LocalDate): List<Showing> {
    val dayText = day.toString()
    return data.filter { it.title.isNotBlank() }.flatMap { movie ->
        movie.screens
            .flatMap { variantsByFormat -> variantsByFormat.entries }
            .flatMap { (screen, variants) -> variants.map { variant -> variant.toFormat(screen) to variant } }
            .flatMap { (format, variant) ->
                variant.performances
                    .filter { it.day == dayText && it.actualStartAt.isNotBlank() }
                    .map { Showing(cleanTitle(movie.title), day, LocalTime.parse(it.actualStartAt), Cinema.UCI, format, movie.poster?.takeIf { it.isNotBlank() }) }
            }
    }
}

fun TheSpaceFilmsDto.toShowings(day: LocalDate): List<Showing> {
    val dayText = day.toString()
    return result.filter { it.filmTitle.isNotBlank() }.flatMap { film ->
        film.showingGroups
            .flatMap { group -> group.sessions }
            .filter { it.startTime.take(10) == dayText && it.startTime.length >= 16 }
            .map {
                Showing(
                    cleanTitle(film.filmTitle), day, LocalTime.parse(it.startTime.substring(11, 16)), Cinema.THE_SPACE,
                    it.toFormat(), film.posterImageSrc?.takeIf { poster -> poster.isNotBlank() }
                )
            }
    }
}

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
