package it.cinemerick.feature.showtimes.domain

import it.cinemerick.core.domain.DataError
import it.cinemerick.core.domain.Result

data class CinemaShowings(
    val cinema: Cinema,
    val result: Result<List<Showing>, DataError.Network>
)
