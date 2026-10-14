package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbMapperTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }

    private fun video(key: String, language: String, type: String = "Trailer", site: String = "YouTube", official: Boolean = false) =
        TmdbVideoDto(key = key, site = site, type = type, official = official, language = language)

    private fun movie(vararg videos: TmdbVideoDto) = TmdbMovieDto(videos = TmdbVideosDto(videos.toList()))

    @Test
    fun mapsAFullMovie() {
        val details = json.decodeFromString<TmdbMovieDto>(MOVIE_JSON).toFilmDetails()

        assertEquals(
            FilmDetails(
                synopsis = "La principessa Leia viene catturata.",
                genre = "Avventura, Azione",
                director = "George Lucas",
                cast = "Mark Hamill, Harrison Ford",
                rating = "Per tutti",
                trailerUrl = "https://www.youtube.com/watch?v=abc123",
                runtimeMinutes = 121,
                year = 1977,
                originalTitle = "Star Wars",
                score = FilmScore(8.207, 22999)
            ),
            details
        )
    }

    @Test
    fun anOfficialItalianTrailerWinsOverTheOthers() {
        val details = movie(video("it1", "it"), video("it2", "it", official = true), video("it3", "it")).toFilmDetails()

        assertEquals("https://www.youtube.com/watch?v=it2", details.trailerUrl)
    }

    @Test
    fun aTrailerOnlyInAnotherLanguageIsNotUsed() {
        assertNull(movie(video("en1", "en", official = true)).toFilmDetails().trailerUrl)
        assertEquals("https://www.youtube.com/watch?v=it1", movie(video("en1", "en"), video("it1", "it")).toFilmDetails().trailerUrl)
    }

    @Test
    fun aBlankSynopsisStaysMissing() {
        assertNull(TmdbMovieDto(overview = "  ").toFilmDetails().synopsis)
        assertNull(TmdbMovieDto(overview = null).toFilmDetails().synopsis)
        assertEquals("Trama", TmdbMovieDto(overview = "Trama").toFilmDetails().synopsis)
    }

    @Test
    fun nonYouTubeNonTrailerAndOtherLanguageVideosAreIgnored() {
        val details = movie(
            video("v1", "it", site = "Vimeo"),
            video("t1", "it", type = "Teaser"),
            video("c1", "en", type = "Clip"),
            video("j1", "ja"),
            video("", "it")
        ).toFilmDetails()

        assertNull(details.trailerUrl)
    }

    @Test
    fun directorsAreJoinedAndOnlyDirectorsCount() {
        val movie = TmdbMovieDto(
            credits = TmdbCreditsDto(
                crew = listOf(
                    TmdbCrewDto("Lana Wachowski", "Director"),
                    TmdbCrewDto("Lilly Wachowski", "Director"),
                    TmdbCrewDto("Lana Wachowski", "Director"),
                    TmdbCrewDto("Joel Silver", "Producer")
                )
            )
        )

        assertEquals("Lana Wachowski, Lilly Wachowski", movie.toFilmDetails().director)
    }

    @Test
    fun castIsCappedAtEight() {
        val movie = TmdbMovieDto(credits = TmdbCreditsDto(cast = (1..12).map { TmdbCastDto("Actor $it") }))

        assertEquals((1..8).joinToString(", ") { "Actor $it" }, movie.toFilmDetails().cast)
    }

    @Test
    fun readsTheItalianCertificationAndSkipsBlankOnes() {
        fun certified(vararg byCountry: Pair<String, List<String>>) = TmdbMovieDto(
            releaseDates = TmdbReleaseDatesDto(
                byCountry.map { (country, certs) -> TmdbCountryReleasesDto(country, certs.map { TmdbReleaseDateDto(it) }) }
            )
        ).toFilmDetails().rating

        assertEquals("14+", certified("US" to listOf("R"), "IT" to listOf("", "VM14")))
        assertEquals("18+", certified("IT" to listOf("VM18")))
        assertEquals("BA", certified("IT" to listOf("BA")))
        assertNull(certified("US" to listOf("PG")))
        assertNull(certified("IT" to listOf("", "")))
    }

    @Test
    fun scoreIsAbsentWithoutVotes() {
        assertNull(TmdbMovieDto(voteAverage = 0.0, voteCount = 0).toFilmDetails().score)
        assertEquals(FilmScore(6.5, 3), TmdbMovieDto(voteAverage = 6.5, voteCount = 3).toFilmDetails().score)
    }

    @Test
    fun yearAndRuntimeAreMappedAndBadValuesDropped() {
        val ok = TmdbMovieDto(releaseDate = "2026-09-01", runtime = 95).toFilmDetails()
        assertEquals(2026, ok.year)
        assertEquals(95, ok.runtimeMinutes)

        val bad = TmdbMovieDto(releaseDate = "", runtime = 0).toFilmDetails()
        assertNull(bad.year)
        assertNull(bad.runtimeMinutes)
        assertNull(TmdbMovieDto(releaseDate = null, runtime = null).toFilmDetails().year)
    }

    @Test
    fun anEmptyMovieGivesEmptyDetails() {
        assertEquals(true, TmdbMovieDto().toFilmDetails().isEmpty)
    }
}
