package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result

data class CinemaShowings(
    val cinema: Cinema,
    val result: Result<List<Showing>, DataError.Network>
)
