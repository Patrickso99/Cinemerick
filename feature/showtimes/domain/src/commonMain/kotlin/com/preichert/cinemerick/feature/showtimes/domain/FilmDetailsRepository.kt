package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result

/** An external film database that can describe a film by its title. */
interface FilmLookup {
    /** The film's details, or `Success(null)` when the database has no confident match for [searchTitle]. */
    suspend fun find(searchTitle: String): Result<FilmDetails?, DataError.Network>

    /** The database's id of the film matching [searchTitle], or `Success(null)` when there is no confident match. */
    suspend fun movieId(searchTitle: String): Result<Int?, DataError.Network>
}

interface FilmDetailsRepository {
    /**
     * The details of the film called [title]: the external database's when it knows the film, otherwise what the
     * cinemas publish ([known] from the showtimes, completed from the film pages in [sources]).
     */
    suspend fun getFilmDetails(title: String, known: FilmDetails, sources: List<DetailsSource>): FilmDetails

    /**
     * The external database's details of the film called [title] alone. `Success(null)` is a confident "no match", while
     * a `Failure` means the database could not be asked, so the answer is not known yet. Nothing is taken from the
     * cinemas, and each film is asked for once per session.
     */
    suspend fun getDatabaseDetails(title: String): Result<FilmDetails?, DataError.Network>

    /**
     * The external database's id of the film called [title]. `Success(null)` is a confident "no match", while a `Failure`
     * means the database could not be asked. Two titles with the same id are the same film, whatever the cinema called them.
     */
    suspend fun getMovieId(title: String): Result<Int?, DataError.Network>
}
