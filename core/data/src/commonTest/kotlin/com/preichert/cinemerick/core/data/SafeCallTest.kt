package com.preichert.cinemerick.core.data

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.test.Test

class SafeCallTest {

    @Serializable
    private data class Film(val title: String)

    private fun clientAnswering(status: HttpStatusCode, body: String = "") = HttpClient(
        MockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) }
    ) { install(ContentNegotiation) { json() } }

    private fun clientThrowing(exception: Throwable) = HttpClient(MockEngine { throw exception })

    @Test
    fun aSuccessfulAnswerIsDecoded() = runTest {
        val result = clientAnswering(HttpStatusCode.OK, """{"title":"Dune"}""").get<Film>("https://cinema.test")

        assertThat(result).isEqualTo(Result.Success(Film("Dune")))
    }

    @Test
    fun everyErrorStatusBecomesItsOwnFailure() = runTest {
        val expected = mapOf(
            400 to DataError.Network.BAD_REQUEST,
            401 to DataError.Network.UNAUTHORIZED,
            403 to DataError.Network.FORBIDDEN,
            404 to DataError.Network.NOT_FOUND,
            408 to DataError.Network.REQUEST_TIMEOUT,
            409 to DataError.Network.CONFLICT,
            429 to DataError.Network.TOO_MANY_REQUESTS,
            500 to DataError.Network.SERVER_ERROR,
            503 to DataError.Network.SERVICE_UNAVAILABLE,
            599 to DataError.Network.SERVER_ERROR,
            418 to DataError.Network.UNKNOWN,
            302 to DataError.Network.UNKNOWN,
        )

        expected.forEach { (status, error) ->
            val result = clientAnswering(HttpStatusCode.fromValue(status)).getResponse("https://cinema.test")

            assertThat(result, name = "status=$status").isEqualTo(Result.Failure(error))
        }
    }

    @Test
    fun theEdgesOfTheSuccessRangeAreSuccesses() = runTest {
        listOf(200, 299).forEach { status ->
            val result = clientAnswering(HttpStatusCode.fromValue(status)).getResponse("https://cinema.test")

            assertThat(result is Result.Success, name = "status=$status").isEqualTo(true)
        }
    }

    @Test
    fun aBodyThatDoesNotDecodeIsASerializationFailure() = runTest {
        val result = clientAnswering(HttpStatusCode.OK, """{"name":"Dune"}""").get<Film>("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.SERIALIZATION))
    }

    @Test
    fun timeoutsBecomeRequestTimeout() = runTest {
        val timeouts = listOf(
            HttpRequestTimeoutException("https://cinema.test", 30_000),
            ConnectTimeoutException("connect timed out"),
            SocketTimeoutException("socket timed out"),
        )

        timeouts.forEach { exception ->
            val result = clientThrowing(exception).getResponse("https://cinema.test")

            assertThat(result, name = exception::class.simpleName).isEqualTo(Result.Failure(DataError.Network.REQUEST_TIMEOUT))
        }
    }

    @Test
    fun anAddressThatDoesNotResolveIsNoInternet() = runTest {
        val result = clientThrowing(UnresolvedAddressException()).getResponse("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.NO_INTERNET))
    }

    @Test
    fun aSerializationExceptionWhileRequestingIsASerializationFailure() = runTest {
        val result = clientThrowing(SerializationException("bad body")).getResponse("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.SERIALIZATION))
    }

    @Test
    fun anUnexpectedExceptionIsAnUnknownFailure() = runTest {
        val result = clientThrowing(IllegalStateException("boom")).getResponse("https://cinema.test")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.UNKNOWN))
    }

    @Test
    fun aCancellationIsThrownAgainNotTurnedIntoAFailure() = runTest {
        assertFailure {
            clientThrowing(CancellationException("cancelled")).getResponse("https://cinema.test")
        }.isInstanceOf<CancellationException>()
    }
}
