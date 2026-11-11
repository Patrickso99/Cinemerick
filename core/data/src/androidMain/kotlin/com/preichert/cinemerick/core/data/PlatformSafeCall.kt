package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Network>
): Result<T, DataError.Network> {
    return try {
        handleResponse(execute())
    } catch (e: HttpRequestTimeoutException) {
        logFailure("Request timed out", e)
        Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: ConnectTimeoutException) {
        // A ConnectException on the JVM, so it is caught before the "no internet" cases.
        logFailure("Connection timed out", e)
        Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SocketTimeoutException) {
        logFailure("Socket timed out", e)
        Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: UnknownHostException) {
        logFailure("No internet", e)
        Result.Failure(DataError.Network.NO_INTERNET)
    } catch (e: UnresolvedAddressException) {
        logFailure("No internet", e)
        Result.Failure(DataError.Network.NO_INTERNET)
    } catch (e: ConnectException) {
        logFailure("No internet", e)
        Result.Failure(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        logFailure("Serialization failed", e)
        Result.Failure(DataError.Network.SERIALIZATION)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logFailure("Request failed", e)
        Result.Failure(DataError.Network.UNKNOWN)
    }
}
