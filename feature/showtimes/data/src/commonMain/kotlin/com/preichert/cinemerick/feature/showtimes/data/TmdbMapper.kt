package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonPrimitive

private const val MAX_CAST = 8
private const val ITALIAN = "it"
private const val YOUTUBE = "YouTube"
private const val TRAILER = "Trailer"
private const val ITALY = "IT"
private const val IMAGE_BASE = "https://image.tmdb.org/t/p"
private const val PROFILE_SIZE = "w185"
private const val BACKDROP_SIZE = "w780"
private const val GALLERY_THUMB_SIZE = "w300"
private const val GALLERY_FULL_SIZE = "w1280"
private const val MAX_GALLERY = 9
private const val THEATRICAL = 3

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
    score = FilmScore(voteAverage, voteCount).takeIf { voteCount > 0 },
    directors = directorPeople(),
    castMembers = castPeople(),
    tagline = tagline.orNullIfBlank(),
    backdropUrl = backdropPath.imageUrl(BACKDROP_SIZE),
    saga = collection?.name.orNullIfBlank(),
    originalLanguage = originalLanguage.orNullIfBlank(),
    countries = productionCountries.mapNotNull { it.name.orNullIfBlank() }.distinct().joinedOrNull(),
    productionCompanies = productionCompanies.mapNotNull { it.name.orNullIfBlank() }.distinct().joinedOrNull(),
    budget = budget.takeIf { it > 0 },
    revenue = revenue.takeIf { it > 0 },
    releaseDate = italianTheatricalDate() ?: releaseDate.toDateOrNull(),
    gallery = gallery()
)

// The earliest Italian theatrical release (a premiere or a re-release may come later); null if there is none.
private fun TmdbMovieDto.italianTheatricalDate(): LocalDate? =
    releaseDates?.results.orEmpty()
        .filter { it.country == ITALY }
        .flatMap { it.releaseDates }
        .filter { it.type == THEATRICAL }
        .mapNotNull { it.releaseDate.toDateOrNull() }
        .minOrNull()

// TMDB writes "2024-02-28" for the primary date and "2024-02-28T00:00:00.000Z" for the dated entries.
private fun String?.toDateOrNull(): LocalDate? = this?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

// The best rated backdrops, apart from the one already shown at the top of the sheet.
private fun TmdbMovieDto.gallery(): List<FilmImage> =
    images?.backdrops.orEmpty()
        .filter { it.filePath.isNotBlank() && it.filePath != backdropPath }
        .sortedByDescending { it.voteAverage }
        .distinctBy { it.filePath }
        .take(MAX_GALLERY)
        .mapNotNull { image ->
            val thumb = image.filePath.imageUrl(GALLERY_THUMB_SIZE)
            val full = image.filePath.imageUrl(GALLERY_FULL_SIZE)
            if (thumb != null && full != null) FilmImage(thumb, full) else null
        }

private fun String?.imageUrl(size: String): String? = orNullIfBlank()?.let { "$IMAGE_BASE/$size${if (it.startsWith("/")) it else "/$it"}" }

private fun TmdbMovieDto.directorPeople(): List<FilmPerson> =
    credits?.crew.orEmpty()
        .filter { it.job == "Director" && it.name.isNotBlank() }
        .distinctBy { it.id to it.name }
        .map { FilmPerson(it.id, it.name.trim(), photoUrl = it.profilePath.imageUrl(PROFILE_SIZE)) }

private fun TmdbMovieDto.castPeople(): List<FilmPerson> =
    credits?.cast.orEmpty()
        .filter { it.name.isNotBlank() }
        .take(MAX_CAST)
        .map { FilmPerson(it.id, it.name.trim(), it.character.orNullIfBlank(), it.profilePath.imageUrl(PROFILE_SIZE)) }

// An Italian trailer on YouTube, an official one first; trailers in other languages and on other sites are not used.
private fun TmdbMovieDto.trailerUrl(): String? =
    videos?.results.orEmpty()
        .filter { it.site == YOUTUBE && it.type == TRAILER && it.key.isNotBlank() && it.language == ITALIAN }
        .sortedBy { !it.official }
        .firstOrNull()
        ?.let { "https://www.youtube.com/watch?v=${it.key}" }

// The first filled certification of Italy, worded like the one The Space publishes ("Per tutti", "14+"), followed by the
// descriptors of that same entry when TMDB has some ("14+ · Violence, Weapons").
private fun TmdbMovieDto.italianCertification(): String? {
    val entry = releaseDates?.results.orEmpty()
        .firstOrNull { it.country == ITALY }
        ?.releaseDates.orEmpty()
        .firstOrNull { it.certification.orNullIfBlank() != null }
        ?: return null
    val certification = entry.certification.trim().let { value ->
        when (value.uppercase()) {
            "T" -> "Per tutti"
            "VM14" -> "14+"
            "VM18" -> "18+"
            else -> value
        }
    }
    val descriptors = entry.descriptors
        .mapNotNull { (it as? JsonPrimitive)?.takeIf { primitive -> primitive.isString }?.content.orNullIfBlank() }
        .distinct()
        .joinedOrNull()
    return if (descriptors == null) certification else "$certification · $descriptors"
}
