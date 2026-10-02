package com.preichert.cinemerick.feature.showtimes.domain

data class FilmGroup(
    val title: String,
    val showings: List<Showing>,
    val posterUrl: String? = null
)

fun List<Showing>.groupByFilm(): List<FilmGroup> {
    val showingOrder = compareBy<Showing>({ it.day }, { it.time }, { it.venue.name })

    return groupBy { filmKey(it.title) }
        .entries
        .sortedBy { it.key }
        .map { (_, showings) ->
            val sorted = showings.sortedWith(showingOrder)
            // Use shortest original title as group header, keep original titles for each showing
            val groupTitle = sorted.map { it.title }.minBy { it.length }
            FilmGroup(title = groupTitle, showings = sorted, posterUrl = sorted.posterUrl())
        }
}

// UCI posters are preferred; others are the fallback.
private fun List<Showing>.posterUrl(): String? =
    (firstOrNull { it.venue.chain == Chain.UCI && it.posterUrl != null } ?: firstOrNull { it.posterUrl != null })?.posterUrl
