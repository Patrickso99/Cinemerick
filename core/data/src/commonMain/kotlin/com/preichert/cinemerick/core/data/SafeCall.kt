package com.preichert.cinemerick.core.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

private val log = Logger.withTag("SafeCall")

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

suspend inline fun HttpClient.getResponse(
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
    execute: () -> HttpResponse
): Result<T, DataError.Network> {
    return when (val result = safeResponse(execute)) {
        is Result.Error -> result
        is Result.Success -> try {
            Result.Success(result.data.body<T>())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logFailure("Deserialization failed", e)
            Result.Error(DataError.Network.SERIALIZATION)
        }
    }
}

suspend inline fun safeResponse(
    execute: () -> HttpResponse
): Result<HttpResponse, DataError.Network> {
    val response = try {
        execute()
    } catch (e: UnresolvedAddressException) {
        logFailure("No internet", e)
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SerializationException) {
        logFailure("Serialization failed", e)
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        // Browser engines (CORS, offline) throw Errors, not Exceptions.
        logFailure("Request failed", e)
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return statusToResult(response)
}

@PublishedApi
internal fun logFailure(message: String, throwable: Throwable) = log.w(throwable) { message }

fun statusToResult(response: HttpResponse): Result<HttpResponse, DataError.Network> {
    if (response.status.value !in 200..299) log.w { "HTTP ${response.status.value}" }
    return when (response.status.value) {
        in 200..299 -> Result.Success(response)
        400 -> Result.Error(DataError.Network.BAD_REQUEST)
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Network.CONFLICT)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}
