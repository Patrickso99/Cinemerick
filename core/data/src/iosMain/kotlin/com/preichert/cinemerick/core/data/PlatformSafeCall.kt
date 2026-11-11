package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import io.ktor.client.engine.darwin.DarwinHttpRequestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut

actual suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Network>
): Result<T, DataError.Network> {
    return try {
        handleResponse(execute())
    } catch (e: DarwinHttpRequestException) {
        logFailure("Request failed", e)
        Result.Failure(e.toNetworkError())
    } catch (e: HttpRequestTimeoutException) {
        logFailure("Request timed out", e)
        Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: ConnectTimeoutException) {
        logFailure("Connection timed out", e)
        Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: SocketTimeoutException) {
        logFailure("Socket timed out", e)
        Result.Failure(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: UnresolvedAddressException) {
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

private fun DarwinHttpRequestException.toNetworkError(): DataError.Network {
    val nsError = origin
    if (nsError.domain != NSURLErrorDomain) return DataError.Network.UNKNOWN
    return when (nsError.code) {
        NSURLErrorNotConnectedToInternet,
        NSURLErrorNetworkConnectionLost,
        NSURLErrorCannotFindHost,
        NSURLErrorDNSLookupFailed,
        NSURLErrorCannotConnectToHost -> DataError.Network.NO_INTERNET
        NSURLErrorTimedOut -> DataError.Network.REQUEST_TIMEOUT
        else -> DataError.Network.UNKNOWN
    }
}
