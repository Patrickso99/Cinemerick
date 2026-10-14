package com.preichert.cinemerick.feature.showtimes.domain

/** User score of a film on TMDB: [value] out of 10, averaged over [votes] votes. */
data class FilmScore(val value: Double, val votes: Int)

/**
 * What is known about a film. Every field is optional: chains disagree on what they offer, and TMDB may not know a film.
 * [runtimeMinutes], [year], [originalTitle] and [score] only ever come from TMDB.
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
    val score: FilmScore? = null
) {
    val isEmpty: Boolean
        get() = synopsis == null && genre == null && director == null && cast == null && rating == null && trailerUrl == null &&
            runtimeMinutes == null && year == null && originalTitle == null && score == null

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
        score = score ?: fallback.score
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
