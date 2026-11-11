package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue

class ResultMergingTest {

    @Test
    fun mergesMultipleSuccesses() {
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Success(listOf(1, 2)),
            Result.Success(listOf(3)),
            Result.Success(listOf(4, 5))
        )

        val merged = results.mergeResults()

        assertThat(merged is Result.Success).isTrue()
        assertThat((merged as Result.Success).data).isEqualTo(listOf(1, 2, 3, 4, 5))
    }

    @Test
    fun ignoresSomeFailuresWhenSomeSucceed() {
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Success(listOf(1, 2)),
            Result.Failure(DataError.Network.REQUEST_TIMEOUT),
            Result.Success(listOf(3))
        )

        val merged = results.mergeResults()

        assertThat(merged is Result.Success).isTrue()
        assertThat((merged as Result.Success).data).isEqualTo(listOf(1, 2, 3))
    }

    @Test
    fun returnsFirstFailureWhenAllFail() {
        val timeout = DataError.Network.REQUEST_TIMEOUT
        val serverError = DataError.Network.SERVER_ERROR
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Failure(timeout),
            Result.Failure(serverError)
        )

        val merged = results.mergeResults()

        assertThat(merged is Result.Failure).isTrue()
        assertThat((merged as Result.Failure).error).isEqualTo(timeout)
    }

    @Test
    fun returnsEmptySuccessWhenListIsEmpty() {
        val results: List<Result<List<Int>, DataError.Network>> = emptyList()

        val merged = results.mergeResults()

        assertThat(merged is Result.Success).isTrue()
        assertThat((merged as Result.Success).data).isEmpty()
    }

    @Test
    fun returnsSuccessesWithEmptyListWhenNoFailures() {
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Success(emptyList<Int>()),
            Result.Success(listOf(1))
        )

        val merged = results.mergeResults()

        assertThat(merged is Result.Success).isTrue()
        assertThat((merged as Result.Success).data).isEqualTo(listOf(1))
    }
}
