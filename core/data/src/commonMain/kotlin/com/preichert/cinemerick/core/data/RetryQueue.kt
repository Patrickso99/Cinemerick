package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val log = Logger.withTag("RetryQueue")

private fun DataError.Network.isRetryable() = when (this) {
    DataError.Network.TOO_MANY_REQUESTS,
    DataError.Network.REQUEST_TIMEOUT,
    DataError.Network.SERVICE_UNAVAILABLE,
    DataError.Network.SERVER_ERROR -> true
    else -> false
}

data class RetryableRequest<T>(
    val id: String,
    val execute: suspend () -> Result<T, DataError.Network>,
    val onResult: (Result<T, DataError.Network>) -> Unit,
    var attempts: Int = 0,
    var nextRetryTime: Long = 0
)

class RetryQueue(private val scope: CoroutineScope) {
    private val queue = mutableMapOf<String, RetryableRequest<*>>()
    private var processingJob: kotlinx.coroutines.Job? = null
    private val clock = Clock.System

    fun <T> enqueue(
        id: String,
        execute: suspend () -> Result<T, DataError.Network>,
        onResult: (Result<T, DataError.Network>) -> Unit
    ) {
        val request = RetryableRequest(id, execute, onResult)
        queue[id] = request
        startProcessing()
    }

    fun cancel(id: String) {
        queue.remove(id)
        log.d { "Cancelled retry for $id" }
    }

    private fun startProcessing() {
        if (processingJob?.isActive == true) return
        processingJob = scope.launch {
            try {
                while (queue.isNotEmpty()) {
                    val now = clock.now().toEpochMilliseconds()
                    val nextRetry = queue.values.filter { it.nextRetryTime <= now }.firstOrNull()

                    if (nextRetry == null) {
                        val nextScheduled = queue.values.minOfOrNull { it.nextRetryTime } ?: return@launch
                        delay((nextScheduled - now).milliseconds)
                        continue
                    }

                    processRequest(nextRetry)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                log.e(e) { "Retry queue processing error" }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processRequest(request: RetryableRequest<*>) {
        request.attempts++
        log.d { "Retrying ${request.id} (attempt ${request.attempts})" }

        val result = request.execute()
        when {
            result is Result.Success<*> -> {
                queue.remove(request.id)
                (request.onResult as (Result<Any, DataError.Network>) -> Unit)(result as Result<Any, DataError.Network>)
            }
            result is Result.Failure && result.error.isRetryable() && request.attempts < MAX_ATTEMPTS -> {
                request.nextRetryTime = clock.now().toEpochMilliseconds() + getBackoffDelay(request.attempts).inWholeMilliseconds
                log.d { "Scheduled retry for ${request.id} in ${getBackoffDelay(request.attempts)}" }
            }
            result is Result.Failure -> {
                queue.remove(request.id)
                (request.onResult as (Result<Any, DataError.Network>) -> Unit)(result as Result<Any, DataError.Network>)
            }
        }
    }

    private fun getBackoffDelay(attempt: Int): Duration {
        val baseDelay = 1000L * (1 shl (attempt - 1)) // 1s, 2s, 4s, 8s, etc.
        val maxDelay = 30000L // 30s max
        return (baseDelay.coerceAtMost(maxDelay)).milliseconds
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
    }
}
