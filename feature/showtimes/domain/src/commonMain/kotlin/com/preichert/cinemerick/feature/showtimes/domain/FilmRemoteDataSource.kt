package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result

/** An external film database that can describe a film by its title. */
interface FilmRemoteDataSource {
    /** The film's details, or `Success(null)` when the database has no confident match for [searchTitle]. */
    suspend fun find(searchTitle: String): Result<FilmDetails?, DataError.Network>

    /** The database's id of the film matching [searchTitle], or `Success(null)` when there is no confident match. */
    suspend fun movieId(searchTitle: String): Result<Int?, DataError.Network>
}
