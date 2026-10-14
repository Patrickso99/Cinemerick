package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.data.HttpClientFactory
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KtorTmdbFilmLookupTest {

    private val today = LocalDate(2026, 10, 14)
    private val requests = mutableListOf<HttpRequestData>()

    private fun lookup(token: String = "secret-token", handler: (HttpRequestData) -> Pair<HttpStatusCode, String>): KtorTmdbFilmLookup {
        val engine = MockEngine { request ->
            requests += request
            val (status, body) = handler(request)
            if (status.value >= 400) respondError(status) else {
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        return KtorTmdbFilmLookup(HttpClientFactory.create(engine), token) { today }
    }

    private fun ok(body: String) = HttpStatusCode.OK to body

    private fun detailsOf(result: Result<FilmDetails?, DataError.Network>): FilmDetails =
        assertNotNull(assertIs<Result.Success<FilmDetails?>>(result).data)

    private fun route(request: HttpRequestData, movie: String = MOVIE_JSON) =
        if (request.url.encodedPath.endsWith("/search/movie")) ok(SEARCH_JSON) else ok(movie)

    @Test
    fun searchesThenFetchesTheDetailOfTheMatch() = runTest {
        val result = lookup { route(it) }.find("Star Wars")

        val details = detailsOf(result)
        assertEquals("La principessa Leia viene catturata.", details.synopsis)
        assertEquals("George Lucas", details.director)
        assertEquals(121, details.runtimeMinutes)

        assertEquals(2, requests.size)
        val search = requests[0].url
        assertEquals("/3/search/movie", search.encodedPath)
        assertEquals("Star Wars", search.parameters["query"])
        assertEquals("it-IT", search.parameters["language"])
        assertEquals("IT", search.parameters["region"])
        assertEquals("false", search.parameters["include_adult"])

        val detail = requests[1].url
        assertEquals("/3/movie/11", detail.encodedPath)
        assertEquals("it-IT", detail.parameters["language"])
        assertEquals("credits,videos,release_dates", detail.parameters["append_to_response"])
        assertEquals("it", detail.parameters["include_video_language"])
    }

    @Test
    fun everyRequestIsInItalianWhateverTheLanguageOfTheApp() = runTest {
        lookup { route(it) }.find("Star Wars")

        assertEquals(listOf("it-IT", "it-IT"), requests.map { it.url.parameters["language"] })
        // Videos too: only Italian ones are asked for.
        assertEquals(listOf("it"), requests.mapNotNull { it.url.parameters["include_video_language"] })
    }

    @Test
    fun everyRequestCarriesTheBearerToken() = runTest {
        lookup { route(it) }.find("Star Wars")

        assertTrue(requests.isNotEmpty())
        requests.forEach { assertEquals("Bearer secret-token", it.headers[HttpHeaders.Authorization]) }
    }

    @Test
    fun aBlankItalianSynopsisIsLeftOutWithoutAskingForAnotherLanguage() = runTest {
        val noOverview = MOVIE_JSON.replace("\"overview\": \"La principessa Leia viene catturata.\",", "\"overview\": \"\",")

        val details = detailsOf(lookup { route(it, movie = noOverview) }.find("Star Wars"))

        assertNull(details.synopsis)
        assertEquals("George Lucas", details.director)
        assertEquals(2, requests.size)
    }

    @Test
    fun noTrustedMatchStopsAfterTheSearch() = runTest {
        val result = lookup { route(it) }.find("Un film che non esiste")

        assertEquals(Result.Success(null), result)
        assertEquals(1, requests.size)
    }

    @Test
    fun blankTokenSendsNothingAndFindsNothing() = runTest {
        val result = lookup(token = "  ") { route(it) }.find("Star Wars")

        assertEquals(Result.Success(null), result)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun blankTitleSendsNothing() = runTest {
        val result = lookup { route(it) }.find("   ")

        assertEquals(Result.Success(null), result)
        assertTrue(requests.isEmpty())
    }

    @Test
    fun aRejectedSearchIsAFailure() = runTest {
        val result = lookup { HttpStatusCode.Unauthorized to "" }.find("Star Wars")

        assertEquals(Result.Failure(DataError.Network.UNAUTHORIZED), result)
        assertEquals(1, requests.size)
    }

    @Test
    fun aRejectedDetailIsAFailure() = runTest {
        val result = lookup {
            if (it.url.encodedPath.endsWith("/search/movie")) ok(SEARCH_JSON) else HttpStatusCode.NotFound to ""
        }.find("Star Wars")

        assertEquals(Result.Failure(DataError.Network.NOT_FOUND), result)
        assertEquals(2, requests.size)
    }
}
