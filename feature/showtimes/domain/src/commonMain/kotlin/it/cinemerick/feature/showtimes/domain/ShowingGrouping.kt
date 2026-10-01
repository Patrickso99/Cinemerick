package it.cinemerick.feature.showtimes.domain

data class FilmGroup(
    val title: String,
    val showings: List<Showing>
)

fun List<Showing>.groupByFilm(): List<FilmGroup> {
    val cleaned = map { it.copy(title = cleanTitle(it.title)) }
    val showingOrder = compareBy<Showing>({ it.day }, { it.time }, { it.cinema.displayName })

    return cleaned
        .groupBy { filmKey(it.title) }
        .entries
        .sortedBy { it.key }
        .map { (_, showings) ->
            val sorted = showings.sortedWith(showingOrder)
            val title = sorted.map { it.title }.minBy { it.length }
            FilmGroup(title = title, showings = sorted.map { it.copy(title = title) })
        }
}
