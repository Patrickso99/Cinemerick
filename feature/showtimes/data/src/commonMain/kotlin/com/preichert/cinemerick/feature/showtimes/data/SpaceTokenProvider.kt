package com.preichert.cinemerick.feature.showtimes.data

import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import com.preichert.cinemerick.core.data.getResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// The Space API wants the JWT that the cinema page hands out as the `microservicesToken` cookie.
class SpaceTokenProvider(
    private val httpClient: HttpClient
) {
    private val mutex = Mutex()
    private var cachedToken: String? = null

    suspend fun getToken(forceRefresh: Boolean = false): Result<String, DataError.Network> =
        mutex.withLock {
            val cached = cachedToken
            if (!forceRefresh && cached != null) return@withLock Result.Success(cached)

            when (val response = httpClient.getResponse(CINEMA_PAGE_URL)) {
                is Result.Error -> response
                is Result.Success -> {
                    val token = response.data.headers.getAll(HttpHeaders.SetCookie).orEmpty()
                        .firstNotNullOfOrNull { TOKEN_REGEX.find(it)?.groupValues?.get(1) }
                    if (token == null) {
                        Result.Error(DataError.Network.UNAUTHORIZED)
                    } else {
                        cachedToken = token
                        Result.Success(token)
                    }
                }
            }
        }

    private companion object {
        const val CINEMA_PAGE_URL = "https://www.thespacecinema.it/cinema/silea/al-cinema"
        val TOKEN_REGEX = Regex("microservicesToken=([^;]+)")
    }
}
