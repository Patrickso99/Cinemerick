package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
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
                score = FilmScore(8.207, 22999),
                directors = listOf(FilmPerson(1, "George Lucas", photoUrl = "https://image.tmdb.org/t/p/w185/george.jpg")),
                castMembers = listOf(
                    FilmPerson(2, "Mark Hamill", "Luke Skywalker", "https://image.tmdb.org/t/p/w185/mark.jpg"),
                    FilmPerson(3, "Harrison Ford", "Han Solo")
                ),
                tagline = "Tanto tempo fa",
                backdropUrl = "https://image.tmdb.org/t/p/w780/backdrop.jpg",
                saga = "Saga di Guerre stellari",
                originalLanguage = "en",
                countries = "United States of America",
                productionCompanies = "Lucasfilm Ltd., Twentieth Century Fox",
                budget = 11_000_000L,
                revenue = 775_398_007L,
                releaseDate = LocalDate(1977, 10, 20),
                gallery = listOf(
                    FilmImage("https://image.tmdb.org/t/p/w300/b2.jpg", "https://image.tmdb.org/t/p/w1280/b2.jpg"),
                    FilmImage("https://image.tmdb.org/t/p/w300/b3.jpg", "https://image.tmdb.org/t/p/w1280/b3.jpg")
                )
            ),
            details
        )
    }

    @Test
    fun aMissingPhotoGivesNoUrlAndAnEmptyCharacterGivesNoRole() {
        val people = TmdbMovieDto(
            credits = TmdbCreditsDto(cast = listOf(TmdbCastDto("A", 1, "", null), TmdbCastDto("B", 2, "  ", ""), TmdbCastDto("", 3, "x", "/x.jpg")))
        ).toFilmDetails().castMembers

        assertEquals(listOf(FilmPerson(1, "A"), FilmPerson(2, "B")), people)
    }

    @Test
    fun castPeopleAreCappedAtEightInOrder() {
        val people = TmdbMovieDto(credits = TmdbCreditsDto(cast = (1..12).map { TmdbCastDto("Actor $it", it) })).toFilmDetails().castMembers

        assertEquals((1..8).toList(), people.map { it.id })
    }

    @Test
    fun blankTaglineSagaAndCountriesStayMissingAndCountriesAreJoined() {
        val blank = TmdbMovieDto(tagline = " ", collection = TmdbCollectionDto(""), productionCountries = listOf(TmdbCountryDto("")), originalLanguage = "")
            .toFilmDetails()
        assertNull(blank.tagline)
        assertNull(blank.saga)
        assertNull(blank.countries)
        assertNull(blank.originalLanguage)
        assertNull(blank.backdropUrl)

        val countries = TmdbMovieDto(productionCountries = listOf(TmdbCountryDto("Italy"), TmdbCountryDto("France"), TmdbCountryDto("Italy"))).toFilmDetails()
        assertEquals("Italy, France", countries.countries)
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

    private fun italianDates(vararg dates: TmdbReleaseDateDto) =
        TmdbReleaseDatesDto(listOf(TmdbCountryReleasesDto("IT", dates.toList())))

    private fun date(type: Int, day: String, certification: String = "", descriptors: List<String> = emptyList()) =
        TmdbReleaseDateDto(certification, type, "${day}T00:00:00.000Z", descriptors.map { JsonPrimitive(it) })

    @Test
    fun zeroOrMissingBudgetAndRevenueGiveNull() {
        val none = TmdbMovieDto(budget = 0, revenue = 0).toFilmDetails()
        assertNull(none.budget)
        assertNull(none.revenue)

        val onlyBudget = TmdbMovieDto(budget = 5_000_000, revenue = 0).toFilmDetails()
        assertEquals(5_000_000L, onlyBudget.budget)
        assertNull(onlyBudget.revenue)
    }

    @Test
    fun theItalianTheatricalDateBeatsThePrimaryOneAndTheEarliestWins() {
        val movie = TmdbMovieDto(
            releaseDate = "2024-03-01",
            releaseDates = italianDates(date(5, "2024-05-23"), date(3, "2024-11-01"), date(3, "2024-02-28"))
        )

        assertEquals(LocalDate(2024, 2, 28), movie.toFilmDetails().releaseDate)
    }

    @Test
    fun withoutAnItalianTheatricalDateThePrimaryDateIsUsed() {
        assertEquals(LocalDate(2024, 3, 1), TmdbMovieDto(releaseDate = "2024-03-01").toFilmDetails().releaseDate)
        assertEquals(
            LocalDate(2024, 3, 1),
            TmdbMovieDto(releaseDate = "2024-03-01", releaseDates = italianDates(date(5, "2024-05-23"))).toFilmDetails().releaseDate
        )
        assertNull(TmdbMovieDto(releaseDate = "").toFilmDetails().releaseDate)
        assertNull(TmdbMovieDto(releaseDate = "not a date").toFilmDetails().releaseDate)
    }

    @Test
    fun theGalleryKeepsTheNineBestBackdropsWithoutTheHeaderOne() {
        val backdrops = (1..12).map { TmdbImageDto("/b$it.jpg", it.toDouble()) } + TmdbImageDto("/header.jpg", 100.0) + TmdbImageDto("", 50.0)
        val details = TmdbMovieDto(backdropPath = "/header.jpg", images = TmdbImagesDto(backdrops)).toFilmDetails()

        assertEquals((12 downTo 4).map { "https://image.tmdb.org/t/p/w1280/b$it.jpg" }, details.gallery.map { it.fullUrl })
        assertEquals("https://image.tmdb.org/t/p/w300/b12.jpg", details.gallery.first().thumbUrl)
    }

    @Test
    fun noExtraBackdropsGiveNoGallery() {
        assertEquals(emptyList(), TmdbMovieDto().toFilmDetails().gallery)
        assertEquals(
            emptyList(),
            TmdbMovieDto(backdropPath = "/h.jpg", images = TmdbImagesDto(listOf(TmdbImageDto("/h.jpg", 5.0)))).toFilmDetails().gallery
        )
    }

    @Test
    fun descriptorsFollowTheCertification() {
        val withDescriptors = TmdbMovieDto(releaseDates = italianDates(date(3, "2024-01-01", "VM14", listOf("Violence", "Weapons", "Violence"))))
        assertEquals("14+ · Violence, Weapons", withDescriptors.toFilmDetails().rating)

        val without = TmdbMovieDto(releaseDates = italianDates(date(3, "2024-01-01", "T")))
        assertEquals("Per tutti", without.toFilmDetails().rating)
    }

    @Test
    fun descriptorsAreTakenFromTheEntryThatProvidesTheCertification() {
        val movie = TmdbMovieDto(
            releaseDates = italianDates(
                date(3, "2024-01-01", "", listOf("Ignored")),
                date(4, "2024-02-01", "6+", listOf("Fear"))
            )
        )

        assertEquals("6+ · Fear", movie.toFilmDetails().rating)
    }

    @Test
    fun descriptorsWithoutACertificationShowNothing() {
        val movie = TmdbMovieDto(releaseDates = italianDates(date(3, "2024-01-01", "", listOf("Violence"))))

        assertNull(movie.toFilmDetails().rating)
    }

    @Test
    fun blankCompaniesAreDroppedAndDuplicatesJoinedOnce() {
        val details = TmdbMovieDto(productionCompanies = listOf(TmdbCompanyDto("A24"), TmdbCompanyDto(" "), TmdbCompanyDto("A24"), TmdbCompanyDto("Blumhouse"))).toFilmDetails()

        assertEquals("A24, Blumhouse", details.productionCompanies)
    }

    @Test
    fun takesTheTmdbPosterAndTheItalianLogo() {
        val details = movie().copy(
            posterPath = "/poster.jpg",
            images = TmdbImagesDto(
                logos = listOf(
                    TmdbImageDto(filePath = "/en.png", voteAverage = 9.0, language = "en"),
                    TmdbImageDto(filePath = "/pt.png", voteAverage = 9.5, language = "pt"),
                    TmdbImageDto(filePath = "/it.png", voteAverage = 5.0, language = "it"),
                    TmdbImageDto(filePath = "/none.png", voteAverage = 8.0, language = null)
                )
            )
        ).toFilmDetails()

        assertEquals("https://image.tmdb.org/t/p/w342/poster.jpg", details.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w500/it.png", details.logoUrl)
    }

    @Test
    fun logoFallsBackToEnglishAndNeverToAnUntaggedOrOtherLanguage() {
        val english = movie().copy(images = TmdbImagesDto(logos = listOf(
            TmdbImageDto(filePath = "/en.png", voteAverage = 2.0, language = "en"),
            TmdbImageDto(filePath = "/none.png", voteAverage = 9.0, language = null),
            TmdbImageDto(filePath = "/pt.png", voteAverage = 9.5, language = "pt")
        ))).toFilmDetails()
        val nothing = movie().copy(images = TmdbImagesDto(logos = listOf(
            TmdbImageDto(filePath = "/none.png", voteAverage = 9.0, language = null),
            TmdbImageDto(filePath = "/pt.png", voteAverage = 9.5, language = "pt")
        ))).toFilmDetails()

        assertEquals("https://image.tmdb.org/t/p/w500/en.png", english.logoUrl)
        assertNull(nothing.logoUrl)
    }

    @Test
    fun galleryKeepsItalianAndUntaggedBackdropsOnly() {
        val details = movie().copy(images = TmdbImagesDto(backdrops = listOf(
            TmdbImageDto(filePath = "/it.jpg", voteAverage = 5.0, language = "it"),
            TmdbImageDto(filePath = "/en.jpg", voteAverage = 9.0, language = "en"),
            TmdbImageDto(filePath = "/none.jpg", voteAverage = 7.0, language = null)
        ))).toFilmDetails()

        assertEquals(listOf("/none.jpg", "/it.jpg"), details.gallery.map { it.fullUrl.substringAfter("w1280") })
    }
}
