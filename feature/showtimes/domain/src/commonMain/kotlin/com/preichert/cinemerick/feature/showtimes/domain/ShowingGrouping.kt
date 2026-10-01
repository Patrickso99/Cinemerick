package com.preichert.cinemerick.feature.showtimes.domain

data class FilmGroup(
    val title: String,
    val showings: List<Showing>
)

fun List<Showing>.groupByFilm(): List<FilmGroup> {
    val showingOrder = compareBy<Showing>({ it.day }, { it.time }, { it.cinema.displayName })

    return groupBy { filmKey(it.title) }
        .entries
        .sortedBy { it.key }
        .map { (_, showings) ->
            val sorted = showings.sortedWith(showingOrder)
            // Use shortest original title as group header, keep original titles for each showing
            val groupTitle = sorted.map { it.title }.minBy { it.length }
            FilmGroup(title = groupTitle, showings = sorted)
        }
}
