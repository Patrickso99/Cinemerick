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
import kotlinx.coroutines.delay
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

fun isTransientError(error: DataError.Network) = when (error) {
    DataError.Network.TOO_MANY_REQUESTS,
    DataError.Network.REQUEST_TIMEOUT,
    DataError.Network.SERVICE_UNAVAILABLE,
    DataError.Network.SERVER_ERROR -> true
    else -> false
}

suspend inline fun <reified T> withRetry(
    maxAttempts: Int = 3,
    crossinline execute: suspend () -> Result<T, DataError.Network>
): Result<T, DataError.Network> {
    var lastError: DataError.Network? = null
    repeat(maxAttempts) { attempt ->
        val result = execute()
        when {
            result is Result.Success<T> -> return result
            result is Result.Error && isTransientError(result.error) -> {
                lastError = result.error
                if (attempt < maxAttempts - 1) {
                    val delayMs = 1000L * (1 shl attempt)
                    delay(delayMs)
                }
            }
            result is Result.Error -> return result
        }
    }
    return Result.Error(lastError ?: DataError.Network.UNKNOWN)
}
