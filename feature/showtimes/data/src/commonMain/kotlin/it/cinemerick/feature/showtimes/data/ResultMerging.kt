package it.cinemerick.feature.showtimes.data

import it.cinemerick.core.domain.DataError
import it.cinemerick.core.domain.Result

// A failing day must not hide the days that worked: fail only when nothing succeeded.
internal fun <T> List<Result<List<T>, DataError.Network>>.mergeResults(): Result<List<T>, DataError.Network> {
    val successes = filterIsInstance<Result.Success<List<T>>>()
    if (successes.isNotEmpty()) return Result.Success(successes.flatMap { it.data })
    val firstError = filterIsInstance<Result.Error<DataError.Network>>().firstOrNull()
    return firstError ?: Result.Success(emptyList())
}
