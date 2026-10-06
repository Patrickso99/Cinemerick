package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResultMergingTest {

    @Test
    fun mergesMultipleSuccesses() {
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Success(listOf(1, 2)),
            Result.Success(listOf(3)),
            Result.Success(listOf(4, 5))
        )

        val merged = results.mergeResults()

        assertTrue(merged is Result.Success)
        assertEquals(listOf(1, 2, 3, 4, 5), (merged as Result.Success).data)
    }

    @Test
    fun ignoresSomeFailuresWhenSomeSucceed() {
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Success(listOf(1, 2)),
            Result.Failure(DataError.Network.REQUEST_TIMEOUT),
            Result.Success(listOf(3))
        )

        val merged = results.mergeResults()

        assertTrue(merged is Result.Success)
        assertEquals(listOf(1, 2, 3), (merged as Result.Success).data)
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

        assertTrue(merged is Result.Failure)
        assertEquals(timeout, (merged as Result.Failure).error)
    }

    @Test
    fun returnsEmptySuccessWhenListIsEmpty() {
        val results: List<Result<List<Int>, DataError.Network>> = emptyList()

        val merged = results.mergeResults()

        assertTrue(merged is Result.Success)
        assertEquals(emptyList(), (merged as Result.Success).data)
    }

    @Test
    fun returnsSuccessesWithEmptyListWhenNoFailures() {
        val results: List<Result<List<Int>, DataError.Network>> = listOf(
            Result.Success(emptyList<Int>()),
            Result.Success(listOf(1))
        )

        val merged = results.mergeResults()

        assertTrue(merged is Result.Success)
        assertEquals(listOf(1), (merged as Result.Success).data)
    }
}
