package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.data.get
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonLookup
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder

private const val TMDB_API = "https://api.themoviedb.org/3"

// Same language as the films: the sheet is in Italian, whatever the language of the app.
private const val LANGUAGE = "it-IT"

/** Looks a person up on TMDB: the profile and the film credits in the same response. An empty [token] turns it off. */
class KtorTmdbPersonLookup(
    private val httpClient: HttpClient,
    private val token: String = BuildKonfig.TMDB_TOKEN
) : PersonLookup {

    override suspend fun find(id: Int): Result<PersonDetails?, DataError.Network> {
        if (token.isBlank()) return Result.Success(null)
        val headers = mapOf(HttpHeaders.Authorization to "Bearer $token", HttpHeaders.Accept to "application/json")
        return when (val result = httpClient.get<TmdbPersonDto>(personUrl(id), headers)) {
            is Result.Failure -> result
            is Result.Success -> Result.Success(result.data.toPersonDetails())
        }
    }

    private fun personUrl(id: Int): String = URLBuilder("$TMDB_API/person/$id").apply {
        parameters.append("language", LANGUAGE)
        parameters.append("append_to_response", "combined_credits")
    }.buildString()
}
