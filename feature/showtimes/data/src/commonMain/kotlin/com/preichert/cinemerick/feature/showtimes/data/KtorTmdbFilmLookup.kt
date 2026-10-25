package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.data.get
import com.preichert.cinemerick.core.domain.BuildKonfig
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmLookup
import com.preichert.cinemerick.feature.showtimes.domain.titleKey
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private val log = Logger.withTag("TmdbFilmLookup")

private const val TMDB_API = "https://api.themoviedb.org/3"

// Everything is requested in Italian: the cinemas list Italian titles and the sheet shows the film's text and trailer
// in Italian, whatever the language of the app. Nothing is requested in another language.
private const val LANGUAGE = "it-IT"
private const val VIDEO_LANGUAGE = "it"
private const val REGION = "IT"
// Italian and English logos, and images without text. The gallery keeps Italian and untagged backdrops only.
private const val IMAGE_LANGUAGES = "it,en,null"

/**
 * Looks a film up on TMDB: a search by title, then the detail of the film it trusts (with credits, trailers, images and the
 * Italian release dates in the same response). The answer of the search is remembered in [matchStore], so a title that was
 * already matched, or already found to have no match, is not searched again. An empty [token] turns the lookup off without
 * sending anything.
 */
class KtorTmdbFilmLookup(
    private val httpClient: HttpClient,
    private val matchStore: TmdbMatchStore,
    private val token: String = BuildKonfig.TMDB_TOKEN,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
) : FilmLookup {

    override suspend fun find(searchTitle: String): Result<FilmDetails?, DataError.Network> =
        when (val id = movieId(searchTitle)) {
            is Result.Failure -> Result.Failure(id.error)
            is Result.Success -> id.data?.let { detailsOf(it) } ?: Result.Success(null)
        }

    override suspend fun movieId(searchTitle: String): Result<Int?, DataError.Network> {
        if (token.isBlank() || searchTitle.isBlank()) return Result.Success(null)
        val key = titleKey(searchTitle)

        when (val remembered = matchStore.get(key)) {
            is RememberedMatch.Found -> return Result.Success(remembered.movieId)
            RememberedMatch.NoMatch -> return Result.Success(null)
            null -> Unit
        }

        val search = when (val result = httpClient.get<TmdbSearchDto>(searchUrl(searchTitle), headers())) {
            is Result.Failure -> return Result.Failure(result.error)
            is Result.Success -> result.data
        }
        val match = search.results.bestMatchFor(searchTitle, today()) ?: run {
            log.d { "No TMDB match for \"$searchTitle\" among ${search.results.size} result(s)" }
            matchStore.put(key, RememberedMatch.NoMatch)
            return Result.Success(null)
        }
        matchStore.put(key, RememberedMatch.Found(match.id))

        return Result.Success(match.id)
    }

    private fun headers() = mapOf(HttpHeaders.Authorization to "Bearer $token", HttpHeaders.Accept to "application/json")

    private suspend fun detailsOf(movieId: Int): Result<FilmDetails?, DataError.Network> =
        when (val result = httpClient.get<TmdbMovieDto>(movieUrl(movieId), headers())) {
            is Result.Failure -> Result.Failure(result.error)
            is Result.Success -> Result.Success(result.data.toFilmDetails())
        }

    private fun searchUrl(title: String): String = URLBuilder("$TMDB_API/search/movie").apply {
        parameters.append("query", title)
        parameters.append("language", LANGUAGE)
        parameters.append("region", REGION)
        parameters.append("include_adult", "false")
    }.buildString()

    private fun movieUrl(id: Int): String = URLBuilder("$TMDB_API/movie/$id").apply {
        parameters.append("language", LANGUAGE)
        parameters.append("append_to_response", "credits,videos,release_dates,images")
        parameters.append("include_video_language", VIDEO_LANGUAGE)
        parameters.append("include_image_language", IMAGE_LANGUAGES)
    }.buildString()
}
