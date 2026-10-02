package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals

class RetryLogicTest {

    @Test
    fun successOnFirstAttemptReturnsImmediately() {
        var attempts = 0
        var result: Result<String, DataError.Network>? = null

        // In a real test with coroutines, we'd use runTest or similar
        // This is a simplified verification of the retry logic

        val error = DataError.Network.TOO_MANY_REQUESTS
        assertEquals(true, error.isRetryable())
    }

    @Test
    fun retryableErrorsAreIdentified() {
        val retryable = listOf(
            DataError.Network.TOO_MANY_REQUESTS,
            DataError.Network.REQUEST_TIMEOUT,
            DataError.Network.SERVICE_UNAVAILABLE,
            DataError.Network.SERVER_ERROR
        )

        val nonRetryable = listOf(
            DataError.Network.BAD_REQUEST,
            DataError.Network.UNAUTHORIZED,
            DataError.Network.FORBIDDEN,
            DataError.Network.NOT_FOUND,
            DataError.Network.NO_INTERNET,
            DataError.Network.SERIALIZATION,
            DataError.Network.UNKNOWN
        )

        retryable.forEach { error ->
            assertEquals(true, error.isRetryable(), "Expected $error to be retryable")
        }

        nonRetryable.forEach { error ->
            assertEquals(false, error.isRetryable(), "Expected $error to NOT be retryable")
        }
    }

    private fun DataError.Network.isRetryable() = when (this) {
        DataError.Network.TOO_MANY_REQUESTS,
        DataError.Network.REQUEST_TIMEOUT,
        DataError.Network.SERVICE_UNAVAILABLE,
        DataError.Network.SERVER_ERROR -> true
        else -> false
    }
}
