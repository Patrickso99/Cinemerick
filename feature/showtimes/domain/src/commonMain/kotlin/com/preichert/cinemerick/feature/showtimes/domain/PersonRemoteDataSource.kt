package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result

/** Where to look a person up in the film database. Null means the database has no such person. */
interface PersonRemoteDataSource {
    suspend fun find(id: Int): Result<PersonDetails?, DataError.Network>
}
