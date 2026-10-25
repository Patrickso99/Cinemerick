package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.data.getResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun HttpClient.fetchFilmPage(
    url: String,
    parse: (String) -> FilmDetails
): Result<FilmDetails, DataError.Network> =
    // The client asks for JSON by default, which the film pages of 18tickets refuse with a 406.
    when (val response = getResponse(url, mapOf(HttpHeaders.Accept to "text/html"))) {
        is Result.Failure -> response
        // Parsing a whole page with regexes is CPU work: keep it off the main thread.
        is Result.Success -> Result.Success(withContext(Dispatchers.Default) { parse(response.data.bodyAsText()) })
    }
