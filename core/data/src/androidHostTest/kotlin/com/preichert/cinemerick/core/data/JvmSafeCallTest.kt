package com.preichert.cinemerick.core.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.Test

class JvmSafeCallTest {

    private fun clientThrowing(exception: Throwable) = HttpClient(MockEngine { throw exception })

    @Test
    fun aHostThatCannotBeFoundIsNoInternet() = runTest {
        val result = clientThrowing(UnknownHostException("cinema.test")).getResponse("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.NO_INTERNET))
    }

    @Test
    fun aRefusedConnectionIsNoInternet() = runTest {
        val result = clientThrowing(ConnectException("Connection refused")).getResponse("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.NO_INTERNET))
    }

    @Test
    fun aSocketThatTimesOutIsARequestTimeout() = runTest {
        val result = clientThrowing(SocketTimeoutException("Read timed out")).getResponse("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.REQUEST_TIMEOUT))
    }
}
