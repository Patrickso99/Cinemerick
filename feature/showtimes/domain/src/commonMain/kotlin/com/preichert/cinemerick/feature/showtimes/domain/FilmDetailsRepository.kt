package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result

/** An external film database that can describe a film by its title. */
interface FilmLookup {
    /** The film's details, or `Success(null)` when the database has no confident match for [searchTitle]. */
    suspend fun find(searchTitle: String): Result<FilmDetails?, DataError.Network>
}

interface FilmDetailsRepository {
    /**
     * The details of the film called [title]: the external database's when it knows the film, otherwise what the
     * cinemas publish ([known] from the showtimes, completed from the film pages in [sources]).
     */
    suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>): FilmDetails
}
