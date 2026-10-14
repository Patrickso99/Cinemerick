package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore

private const val MAX_CAST = 8
private const val ITALIAN = "it"
private const val YOUTUBE = "YouTube"
private const val TRAILER = "Trailer"
private const val ITALY = "IT"

/** TMDB's description of a film, as it was requested: in Italian, so a missing synopsis or trailer stays missing. */
internal fun TmdbMovieDto.toFilmDetails(): FilmDetails = FilmDetails(
    synopsis = overview.orNullIfBlank(),
    genre = genres.mapNotNull { it.name.orNullIfBlank() }.joinedOrNull(),
    director = credits?.crew.orEmpty().filter { it.job == "Director" }.mapNotNull { it.name.orNullIfBlank() }.distinct().joinedOrNull(),
    cast = credits?.cast.orEmpty().mapNotNull { it.name.orNullIfBlank() }.take(MAX_CAST).joinedOrNull(),
    rating = italianCertification(),
    trailerUrl = trailerUrl(),
    runtimeMinutes = runtime?.takeIf { it > 0 },
    year = releaseDate?.take(4)?.toIntOrNull(),
    originalTitle = originalTitle.orNullIfBlank(),
    score = FilmScore(voteAverage, voteCount).takeIf { voteCount > 0 }
)

// An Italian trailer on YouTube, an official one first; trailers in other languages and on other sites are not used.
private fun TmdbMovieDto.trailerUrl(): String? =
    videos?.results.orEmpty()
        .filter { it.site == YOUTUBE && it.type == TRAILER && it.key.isNotBlank() && it.language == ITALIAN }
        .sortedBy { !it.official }
        .firstOrNull()
        ?.let { "https://www.youtube.com/watch?v=${it.key}" }

// The first filled certification of Italy, worded like the one The Space publishes ("Per tutti", "14+").
private fun TmdbMovieDto.italianCertification(): String? =
    releaseDates?.results.orEmpty()
        .firstOrNull { it.country == ITALY }
        ?.releaseDates.orEmpty()
        .firstNotNullOfOrNull { it.certification.orNullIfBlank() }
        ?.let { certification ->
            when (certification.uppercase()) {
                "T" -> "Per tutti"
                "VM14" -> "14+"
                "VM18" -> "18+"
                else -> certification
            }
        }
