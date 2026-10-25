package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonFilm

private const val MAX_PERSON_FILMS = 30
private const val MOVIE = "movie"
private const val PROFILE_SIZE = "h632"
private const val POSTER_SIZE = "w342"

/** TMDB's profile of a person, as it was requested: in Italian, so a missing biography stays missing. */
internal fun TmdbPersonDto.toPersonDetails(): PersonDetails = PersonDetails(
    id = id,
    name = name.trim(),
    biography = biography.orNullIfBlank(),
    birthday = birthday.toDateOrNull(),
    deathday = deathday.toDateOrNull(),
    placeOfBirth = placeOfBirth.orNullIfBlank(),
    photoUrl = profilePath.imageUrl(PROFILE_SIZE),
    films = films()
)

// Cast and crew credits of films, one entry per film, most recent first. A person who both acted and worked behind the
// camera in a film is listed once, with the acting role (crew credits have no role in the app's language).
private fun TmdbPersonDto.films(): List<PersonFilm> {
    val credits = combinedCredits ?: return emptyList()
    return (credits.cast + credits.crew)
        .filter { it.mediaType == MOVIE && it.id != 0 && !it.title.isNullOrBlank() }
        .distinctBy { it.id }
        .sortedByDescending { it.releaseDate.orEmpty() }
        .take(MAX_PERSON_FILMS)
        .map { credit ->
            PersonFilm(
                id = credit.id,
                title = credit.title.orEmpty().trim(),
                year = credit.releaseDate?.take(4)?.toIntOrNull(),
                role = credit.character.orNullIfBlank(),
                posterUrl = credit.posterPath.imageUrl(POSTER_SIZE),
                score = credit.voteAverage.takeIf { it > 0 }
            )
        }
}
