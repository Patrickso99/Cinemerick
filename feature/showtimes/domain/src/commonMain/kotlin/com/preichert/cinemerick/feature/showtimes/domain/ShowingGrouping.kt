package com.preichert.cinemerick.feature.showtimes.domain

data class FilmGroup(
    val title: String,
    val showings: List<Showing>,
    val posterUrl: String? = null,
    val details: FilmDetails = FilmDetails(),
    val detailsSources: List<DetailsSource> = emptyList()
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
