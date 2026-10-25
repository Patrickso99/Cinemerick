package com.preichert.cinemerick.feature.showtimes.domain

data class FilmGroup(
    val title: String,
    val showings: List<Showing>,
    val posterUrl: String? = null,
    val details: FilmDetails = FilmDetails(),
    val detailsSources: List<DetailsSource> = emptyList()
)

// [movieIds] maps a showing's title to its film database id: showings with the same id are the same film, whatever their
// titles. A showing without an id is grouped by its title's `filmKey`, as before.
fun List<Showing>.groupByFilm(movieIds: Map<String, Int> = emptyMap()): List<FilmGroup> {
    val showingOrder = compareBy<Showing>({ it.day }, { it.time }, { it.venue.name })

    return groupBy { movieIds[it.title]?.let { id -> "id:$id" } ?: filmKey(it.title) }
        .entries
        .sortedBy { (_, showings) -> showings.minOf { filmKey(it.title) } }
        .map { (_, showings) ->
            val sorted = showings.sortedWith(showingOrder)
            // Use shortest original title as group header, keep original titles for each showing
            val groupTitle = sorted.map { it.title }.minBy { it.length }
            FilmGroup(
                title = groupTitle,
                showings = sorted,
                posterUrl = sorted.posterUrl(),
                details = sorted.mergedDetails(),
                detailsSources = sorted.detailsSources()
            )
        }
}

// The poster of the highest-priority chain that has one.
private fun List<Showing>.posterUrl(): String? =
    filter { it.posterUrl != null }.minByOrNull { it.venue.chain.priorityRank }?.posterUrl
