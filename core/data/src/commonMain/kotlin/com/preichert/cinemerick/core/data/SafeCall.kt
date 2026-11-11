package com.preichert.cinemerick.core.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException

private val log = Logger.withTag("SafeCall")

/**
 * GET [url] and decode the JSON body into [Response]. For the sources that answer in JSON (UCI, The Space, TMDB).
 * Never throws for a failed request: no connection, an error status or a body that does not decode come back as a
 * [Result.Failure].
 */
suspend inline fun <reified Response : Any> HttpClient.get(
    url: String,
    headers: Map<String, String> = emptyMap()
): Result<Response, DataError.Network> {
    return safeCall {
        get {
            url(url)
            headers.forEach { (name, value) -> header(name, value) }
        }
    }
}

/**
 * GET [url] and return the raw response, without decoding it. For the cinemas whose pages are read as HTML: the caller
 * takes the text with `bodyAsText()` and parses it. Fails the same way as [get], except that the body is not checked.
 */
suspend fun HttpClient.getResponse(
    url: String,
    headers: Map<String, String> = emptyMap()
): Result<HttpResponse, DataError.Network> {
    return safeResponse {
        get {
            url(url)
            headers.forEach { (name, value) -> header(name, value) }
        }
    }
}

suspend inline fun <reified T> safeCall(
    noinline execute: suspend () -> HttpResponse
): Result<T, DataError.Network> {
    return when (val result = safeResponse(execute)) {
        is Result.Failure -> result
        is Result.Success -> try {
            Result.Success(result.data.body<T>())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logFailure("Deserialization failed", e)
            Result.Failure(DataError.Network.SERIALIZATION)
        }
    }
}

suspend fun safeResponse(
    execute: suspend () -> HttpResponse
): Result<HttpResponse, DataError.Network> {
    return platformSafeCall(execute = execute, handleResponse = ::statusToResult)
}

/**
 * Runs [execute] and hands its response to [handleResponse]. A request that throws becomes a failure: which exception
 * means "no internet" or "timed out" differs by engine (OkHttp on Android and desktop, Darwin on iOS), so each platform
 * maps its own. A cancellation is always thrown again.
 */
expect suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Network>
): Result<T, DataError.Network>

@PublishedApi
internal fun logFailure(message: String, throwable: Throwable) {
    log.w(throwable) { message }
}

fun statusToResult(response: HttpResponse): Result<HttpResponse, DataError.Network> {
    if (response.status.value !in 200..299) log.w { "HTTP ${response.status.value}" }
    return when (response.status.value) {
        in 200..299 -> Result.Success(response)
        400 -> Result.Failure(DataError.Network.BAD_REQUEST)
        401 -> Result.Failure(DataError.Network.UNAUTHORIZED)
        403 -> Result.Failure(DataError.Network.FORBIDDEN)
        404 -> Result.Failure(DataError.Network.NOT_FOUND)
        408 -> Result.Failure(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Failure(DataError.Network.CONFLICT)
        429 -> Result.Failure(DataError.Network.TOO_MANY_REQUESTS)
        503 -> Result.Failure(DataError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> Result.Failure(DataError.Network.SERVER_ERROR)
        else -> Result.Failure(DataError.Network.UNKNOWN)
    }
}
