package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.core.testing.FakeClock
import com.preichert.cinemerick.core.data.HttpClientFactory
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.testing.MemorySettings
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
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull

class KtorTmdbFilmDataSourceTest {

    private val today = LocalDate(2026, 10, 14)
    private val requests = mutableListOf<HttpRequestData>()

    private fun lookup(
        token: String = "secret-token",
        store: TmdbMatchLocalDataSource = SettingsTmdbMatchDataSource(MemorySettings(), FakeClock()),
        handler: (HttpRequestData) -> Pair<HttpStatusCode, String>
    ): KtorTmdbFilmDataSource {
        val engine = MockEngine { request ->
            requests += request
            val (status, body) = handler(request)
            if (status.value >= 400) respondError(status) else {
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        return KtorTmdbFilmDataSource(HttpClientFactory.create(engine), store, token) { today }
    }

    private fun ok(body: String) = HttpStatusCode.OK to body

    private fun detailsOf(result: Result<FilmDetails?, DataError.Network>): FilmDetails {
        assertThat(result).isInstanceOf<Result.Success<FilmDetails?>>()
        val details = (result as Result.Success).data
        assertThat(details).isNotNull()
        return details!!
    }

    private fun route(request: HttpRequestData, movie: String = MOVIE_JSON) =
        if (request.url.encodedPath.endsWith("/search/movie")) ok(SEARCH_JSON) else ok(movie)

    @Test
    fun searchesThenFetchesTheDetailOfTheMatch() = runTest {
        val result = lookup { route(it) }.find("Star Wars")

        val details = detailsOf(result)
        assertThat(details.synopsis).isEqualTo("La principessa Leia viene catturata.")
        assertThat(details.director).isEqualTo("George Lucas")
        assertThat(details.runtimeMinutes).isEqualTo(121)

        assertThat(requests.size).isEqualTo(2)
        val search = requests[0].url
        assertThat(search.encodedPath).isEqualTo("/3/search/movie")
        assertThat(search.parameters["query"]).isEqualTo("Star Wars")
        assertThat(search.parameters["language"]).isEqualTo("it-IT")
        assertThat(search.parameters["region"]).isEqualTo("IT")
        assertThat(search.parameters["include_adult"]).isEqualTo("false")

        val detail = requests[1].url
        assertThat(detail.encodedPath).isEqualTo("/3/movie/11")
        assertThat(detail.parameters["language"]).isEqualTo("it-IT")
        assertThat(detail.parameters["append_to_response"]).isEqualTo("credits,videos,release_dates,images")
        assertThat(detail.parameters["include_video_language"]).isEqualTo("it")
        assertThat(detail.parameters["include_image_language"]).isEqualTo("it,en,null")
    }

    @Test
    fun everyRequestIsInItalianWhateverTheLanguageOfTheApp() = runTest {
        lookup { route(it) }.find("Star Wars")

        assertThat(requests.map { it.url.parameters["language"] }).isEqualTo(listOf("it-IT", "it-IT"))
        // Videos too: only Italian ones are asked for.
        assertThat(requests.mapNotNull { it.url.parameters["include_video_language"] }).isEqualTo(listOf("it"))
    }

    @Test
    fun everyRequestCarriesTheBearerToken() = runTest {
        lookup { route(it) }.find("Star Wars")

        assertThat(requests).isNotEmpty()
        requests.forEach { assertThat(it.headers[HttpHeaders.Authorization]).isEqualTo("Bearer secret-token") }
    }

    @Test
    fun aBlankItalianSynopsisIsLeftOutWithoutAskingForAnotherLanguage() = runTest {
        val noOverview = MOVIE_JSON.replace("\"overview\": \"La principessa Leia viene catturata.\",", "\"overview\": \"\",")

        val details = detailsOf(lookup { route(it, movie = noOverview) }.find("Star Wars"))

        assertThat(details.synopsis).isNull()
        assertThat(details.director).isEqualTo("George Lucas")
        assertThat(requests.size).isEqualTo(2)
    }

    @Test
    fun blankTokenSendsNothingAndFindsNothing() = runTest {
        val result = lookup(token = "  ") { route(it) }.find("Star Wars")

        assertThat(result).isEqualTo(Result.Success(null))
        assertThat(requests).isEmpty()
    }

    @Test
    fun blankTitleSendsNothing() = runTest {
        val result = lookup { route(it) }.find("   ")

        assertThat(result).isEqualTo(Result.Success(null))
        assertThat(requests).isEmpty()
    }

    @Test
    fun aRejectedSearchIsAFailure() = runTest {
        val result = lookup { HttpStatusCode.Unauthorized to "" }.find("Star Wars")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.UNAUTHORIZED))
        assertThat(requests.size).isEqualTo(1)
    }

    @Test
    fun aMatchedTitleFetchesOnlyTheDetailTheNextTime() = runTest {
        val lookup = lookup { route(it) }
        lookup.find("Star Wars")
        requests.clear()

        val details = detailsOf(lookup.find("Star Wars"))

        assertThat(details.director).isEqualTo("George Lucas")
        assertThat(requests.map { it.url.encodedPath }).isEqualTo(listOf("/3/movie/11"))
    }

    @Test
    fun aNoMatchIsNotSearchedAgainUntilItExpires() = runTest {
        val clock = FakeClock()
        val store = SettingsTmdbMatchDataSource(MemorySettings(), clock)
        val lookup = lookup(store = store) { ok("""{"results":[]}""") }
        assertThat(lookup.find("Mammina").let { (it as Result.Success).data }).isNull()
        assertThat(lookup.find("Mammina").let { (it as Result.Success).data }).isNull()
        assertThat(requests.size).isEqualTo(1)

        clock.millis = 8L * 24 * 60 * 60 * 1000
        lookup.find("Mammina")
        assertThat(requests.size).isEqualTo(2)
    }

    @Test
    fun aFailedSearchIsNotRememberedAsNoMatch() = runTest {
        var searchFails = true
        val lookup = lookup {
            if (it.url.encodedPath.endsWith("/search/movie") && searchFails) HttpStatusCode.Unauthorized to ""
            else if (it.url.encodedPath.endsWith("/search/movie")) ok(SEARCH_JSON) else ok(MOVIE_JSON)
        }
        assertThat(lookup.find("Star Wars")).isEqualTo(Result.Failure(DataError.Network.UNAUTHORIZED))

        searchFails = false
        assertThat(detailsOf(lookup.find("Star Wars")).director).isEqualTo("George Lucas")
        assertThat(requests.size).isEqualTo(3)
    }

    @Test
    fun aRejectedDetailIsAFailure() = runTest {
        val result = lookup {
            if (it.url.encodedPath.endsWith("/search/movie")) ok(SEARCH_JSON) else HttpStatusCode.NotFound to ""
        }.find("Star Wars")

        assertThat(result).isEqualTo(Result.Failure(DataError.Network.NOT_FOUND))
        assertThat(requests.size).isEqualTo(2)
    }
}
