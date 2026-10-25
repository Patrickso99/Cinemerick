package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate

/** User score of a film on TMDB: [value] out of 10, averaged over [votes] votes. */
data class FilmScore(val value: Double, val votes: Int)

/**
 * A director or cast member known from TMDB. [role] is the character a cast member plays. [id] is the person's TMDB id,
 * kept for a future person page. [photoUrl] is absolute.
 */
data class FilmPerson(val id: Int, val name: String, val role: String? = null, val photoUrl: String? = null)

/** An extra image of a film: a small [thumbUrl] for the gallery and a larger [fullUrl] for when it is opened. */
data class FilmImage(val thumbUrl: String, val fullUrl: String)

/**
 * What is known about a film. Every field is optional: chains disagree on what they offer, and TMDB may not know a film.
 * [runtimeMinutes], [year], [originalTitle], [score], the people lists, [tagline], [backdropUrl], [saga], [originalLanguage],
 * [countries], [productionCompanies], [budget], [revenue], [releaseDate], [gallery], [posterUrl] and [logoUrl] only ever come
 * from TMDB.
 */
data class FilmDetails(
    val synopsis: String? = null,
    val genre: String? = null,
    val director: String? = null,
    val cast: String? = null,
    val rating: String? = null,
    val trailerUrl: String? = null,
    val runtimeMinutes: Int? = null,
    val year: Int? = null,
    val originalTitle: String? = null,
    val score: FilmScore? = null,
    val directors: List<FilmPerson> = emptyList(),
    val castMembers: List<FilmPerson> = emptyList(),
    val tagline: String? = null,
    val backdropUrl: String? = null,
    val saga: String? = null,
    val originalLanguage: String? = null,
    val countries: String? = null,
    val productionCompanies: String? = null,
    /** In US dollars; null when TMDB has none or reports 0. */
    val budget: Long? = null,
    val revenue: Long? = null,
    val releaseDate: LocalDate? = null,
    val gallery: List<FilmImage> = emptyList(),
    /** TMDB's poster: the home list shows it whenever TMDB knows the film. */
    val posterUrl: String? = null,
    /** TMDB's title logo, in Italian when TMDB has one. */
    val logoUrl: String? = null
) {
    val isEmpty: Boolean
        get() = synopsis == null && genre == null && director == null && cast == null && rating == null && trailerUrl == null &&
            runtimeMinutes == null && year == null && originalTitle == null && score == null &&
            directors.isEmpty() && castMembers.isEmpty() && tagline == null && backdropUrl == null && saga == null &&
            originalLanguage == null && countries == null && productionCompanies == null &&
            budget == null && revenue == null && releaseDate == null && gallery.isEmpty() && posterUrl == null && logoUrl == null

    /** Every field the chains publish is present; the TMDB-only fields are deliberately not part of this. */
    val isComplete: Boolean
        get() = synopsis != null && genre != null && director != null && cast != null && rating != null && trailerUrl != null

    /** Keeps this value's fields and takes only the missing ones from [fallback]. */
    fun withFallback(fallback: FilmDetails): FilmDetails = FilmDetails(
        synopsis = synopsis ?: fallback.synopsis,
        genre = genre ?: fallback.genre,
        director = director ?: fallback.director,
        cast = cast ?: fallback.cast,
        rating = rating ?: fallback.rating,
        trailerUrl = trailerUrl ?: fallback.trailerUrl,
        runtimeMinutes = runtimeMinutes ?: fallback.runtimeMinutes,
        year = year ?: fallback.year,
        originalTitle = originalTitle ?: fallback.originalTitle,
        score = score ?: fallback.score,
        directors = directors.ifEmpty { fallback.directors },
        castMembers = castMembers.ifEmpty { fallback.castMembers },
        tagline = tagline ?: fallback.tagline,
        backdropUrl = backdropUrl ?: fallback.backdropUrl,
        saga = saga ?: fallback.saga,
        originalLanguage = originalLanguage ?: fallback.originalLanguage,
        countries = countries ?: fallback.countries,
        productionCompanies = productionCompanies ?: fallback.productionCompanies,
        budget = budget ?: fallback.budget,
        revenue = revenue ?: fallback.revenue,
        releaseDate = releaseDate ?: fallback.releaseDate,
        gallery = gallery.ifEmpty { fallback.gallery },
        posterUrl = posterUrl ?: fallback.posterUrl,
        logoUrl = logoUrl ?: fallback.logoUrl
    )
}

/** Where to fetch a film's details when its chain does not publish them with the showtimes. */
data class DetailsSource(val chain: Chain, val url: String)

/** Chains in the order their data wins when several describe the same film (posters and details alike). */
val chainPriority: List<Chain> = listOf(
    Chain.UCI,
    Chain.THE_SPACE,
    Chain.NOTORIOUS,
    Chain.ARCADIA,
    Chain.CINERGIA,
    Chain.CINEMAZERO,
    Chain.CRISTALLO,
    Chain.DON_BOSCO
)

val Chain.priorityRank: Int
    get() = chainPriority.indexOf(this).let { if (it < 0) chainPriority.size else it }

/** Details already known from the showtimes, merged field by field following [chainPriority]. */
fun List<Showing>.mergedDetails(): FilmDetails =
    filter { it.details != null }
        .sortedBy { it.venue.chain.priorityRank }
        .fold(FilmDetails()) { merged, showing -> merged.withFallback(showing.details!!) }

/** One page to fetch per chain (the first showing's), best chain first. */
fun List<Showing>.detailsSources(): List<DetailsSource> =
    mapNotNull { showing -> showing.detailsUrl?.let { DetailsSource(showing.venue.chain, it) } }
        .distinctBy { it.chain }
        .sortedBy { it.chain.priorityRank }
