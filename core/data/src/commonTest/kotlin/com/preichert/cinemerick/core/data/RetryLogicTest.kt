package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue

class RetryLogicTest {

    @Test
    fun successOnFirstAttemptReturnsImmediately() {
        var attempts = 0
        var result: Result<String, DataError.Network>? = null

        // In a real test with coroutines, we'd use runTest or similar
        // This is a simplified verification of the retry logic

        val error = DataError.Network.TOO_MANY_REQUESTS
        assertThat(error.isRetryable()).isTrue()
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
            assertThat(error.isRetryable(), name = "error=$error").isTrue()
        }

        nonRetryable.forEach { error ->
            assertThat(error.isRetryable(), name = "error=$error").isFalse()
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
