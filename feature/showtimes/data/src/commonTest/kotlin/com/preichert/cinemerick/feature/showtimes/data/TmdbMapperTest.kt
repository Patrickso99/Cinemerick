package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue

class TmdbMapperTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }

    private fun video(key: String, language: String, type: String = "Trailer", site: String = "YouTube", official: Boolean = false) =
        TmdbVideoDto(key = key, site = site, type = type, official = official, language = language)

    private fun movie(vararg videos: TmdbVideoDto) = TmdbMovieDto(videos = TmdbVideosDto(videos.toList()))

    @Test
    fun mapsAFullMovie() {
        val details = json.decodeFromString<TmdbMovieDto>(MOVIE_JSON).toFilmDetails()

        assertThat(details).isEqualTo(FilmDetails(
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
            ))
    }

    @Test
    fun aMissingPhotoGivesNoUrlAndAnEmptyCharacterGivesNoRole() {
        val people = TmdbMovieDto(
            credits = TmdbCreditsDto(cast = listOf(TmdbCastDto("A", 1, "", null), TmdbCastDto("B", 2, "  ", ""), TmdbCastDto("", 3, "x", "/x.jpg")))
        ).toFilmDetails().castMembers

        assertThat(people).isEqualTo(listOf(FilmPerson(1, "A"), FilmPerson(2, "B")))
    }

    @Test
    fun castPeopleAreCappedAtEightInOrder() {
        val people = TmdbMovieDto(credits = TmdbCreditsDto(cast = (1..12).map { TmdbCastDto("Actor $it", it) })).toFilmDetails().castMembers

        assertThat(people.map { it.id }).isEqualTo((1..8).toList())
    }

    @Test
    fun blankTaglineSagaAndCountriesStayMissingAndCountriesAreJoined() {
        val blank = TmdbMovieDto(tagline = " ", collection = TmdbCollectionDto(""), productionCountries = listOf(TmdbCountryDto("")), originalLanguage = "")
            .toFilmDetails()
        assertThat(blank.tagline).isNull()
        assertThat(blank.saga).isNull()
        assertThat(blank.countries).isNull()
        assertThat(blank.originalLanguage).isNull()
        assertThat(blank.backdropUrl).isNull()

        val countries = TmdbMovieDto(productionCountries = listOf(TmdbCountryDto("Italy"), TmdbCountryDto("France"), TmdbCountryDto("Italy"))).toFilmDetails()
        assertThat(countries.countries).isEqualTo("Italy, France")
    }

    @Test
    fun anOfficialItalianTrailerWinsOverTheOthers() {
        val details = movie(video("it1", "it"), video("it2", "it", official = true), video("it3", "it")).toFilmDetails()

        assertThat(details.trailerUrl).isEqualTo("https://www.youtube.com/watch?v=it2")
    }

    @Test
    fun aTrailerOnlyInAnotherLanguageIsNotUsed() {
        assertThat(movie(video("en1", "en", official = true)).toFilmDetails().trailerUrl).isNull()
        assertThat(movie(video("en1", "en"), video("it1", "it")).toFilmDetails().trailerUrl).isEqualTo("https://www.youtube.com/watch?v=it1")
    }

    @Test
    fun aBlankSynopsisStaysMissing() {
        assertThat(TmdbMovieDto(overview = "  ").toFilmDetails().synopsis).isNull()
        assertThat(TmdbMovieDto(overview = null).toFilmDetails().synopsis).isNull()
        assertThat(TmdbMovieDto(overview = "Trama").toFilmDetails().synopsis).isEqualTo("Trama")
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

        assertThat(details.trailerUrl).isNull()
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

        assertThat(movie.toFilmDetails().director).isEqualTo("Lana Wachowski, Lilly Wachowski")
    }

    @Test
    fun castIsCappedAtEight() {
        val movie = TmdbMovieDto(credits = TmdbCreditsDto(cast = (1..12).map { TmdbCastDto("Actor $it") }))

        assertThat(movie.toFilmDetails().cast).isEqualTo((1..8).joinToString(", ") { "Actor $it" })
    }

    @Test
    fun readsTheItalianCertificationAndSkipsBlankOnes() {
        fun certified(vararg byCountry: Pair<String, List<String>>) = TmdbMovieDto(
            releaseDates = TmdbReleaseDatesDto(
                byCountry.map { (country, certs) -> TmdbCountryReleasesDto(country, certs.map { TmdbReleaseDateDto(it) }) }
            )
        ).toFilmDetails().rating

        assertThat(certified("US" to listOf("R"), "IT" to listOf("", "VM14"))).isEqualTo("14+")
        assertThat(certified("IT" to listOf("VM18"))).isEqualTo("18+")
        assertThat(certified("IT" to listOf("BA"))).isEqualTo("BA")
        assertThat(certified("US" to listOf("PG"))).isNull()
        assertThat(certified("IT" to listOf("", ""))).isNull()
    }

    @Test
    fun scoreIsAbsentWithoutVotes() {
        assertThat(TmdbMovieDto(voteAverage = 0.0, voteCount = 0).toFilmDetails().score).isNull()
        assertThat(TmdbMovieDto(voteAverage = 6.5, voteCount = 3).toFilmDetails().score).isEqualTo(FilmScore(6.5, 3))
    }

    @Test
    fun yearAndRuntimeAreMappedAndBadValuesDropped() {
        val ok = TmdbMovieDto(releaseDate = "2026-09-01", runtime = 95).toFilmDetails()
        assertThat(ok.year).isEqualTo(2026)
        assertThat(ok.runtimeMinutes).isEqualTo(95)

        val bad = TmdbMovieDto(releaseDate = "", runtime = 0).toFilmDetails()
        assertThat(bad.year).isNull()
        assertThat(bad.runtimeMinutes).isNull()
        assertThat(TmdbMovieDto(releaseDate = null, runtime = null).toFilmDetails().year).isNull()
    }

    @Test
    fun anEmptyMovieGivesEmptyDetails() {
        assertThat(TmdbMovieDto().toFilmDetails().isEmpty).isTrue()
    }

    private fun italianDates(vararg dates: TmdbReleaseDateDto) =
        TmdbReleaseDatesDto(listOf(TmdbCountryReleasesDto("IT", dates.toList())))

    private fun date(type: Int, day: String, certification: String = "", descriptors: List<String> = emptyList()) =
        TmdbReleaseDateDto(certification, type, "${day}T00:00:00.000Z", descriptors.map { JsonPrimitive(it) })

    @Test
    fun zeroOrMissingBudgetAndRevenueGiveNull() {
        val none = TmdbMovieDto(budget = 0, revenue = 0).toFilmDetails()
        assertThat(none.budget).isNull()
        assertThat(none.revenue).isNull()

        val onlyBudget = TmdbMovieDto(budget = 5_000_000, revenue = 0).toFilmDetails()
        assertThat(onlyBudget.budget).isEqualTo(5_000_000L)
        assertThat(onlyBudget.revenue).isNull()
    }

    @Test
    fun theItalianTheatricalDateBeatsThePrimaryOneAndTheEarliestWins() {
        val movie = TmdbMovieDto(
            releaseDate = "2024-03-01",
            releaseDates = italianDates(date(5, "2024-05-23"), date(3, "2024-11-01"), date(3, "2024-02-28"))
        )

        assertThat(movie.toFilmDetails().releaseDate).isEqualTo(LocalDate(2024, 2, 28))
    }

    @Test
    fun withoutAnItalianTheatricalDateThePrimaryDateIsUsed() {
        assertThat(TmdbMovieDto(releaseDate = "2024-03-01").toFilmDetails().releaseDate).isEqualTo(LocalDate(2024, 3, 1))
        assertThat(TmdbMovieDto(releaseDate = "2024-03-01", releaseDates = italianDates(date(5, "2024-05-23"))).toFilmDetails().releaseDate).isEqualTo(LocalDate(2024, 3, 1))
        assertThat(TmdbMovieDto(releaseDate = "").toFilmDetails().releaseDate).isNull()
        assertThat(TmdbMovieDto(releaseDate = "not a date").toFilmDetails().releaseDate).isNull()
    }

    @Test
    fun theGalleryKeepsTheNineBestBackdropsWithoutTheHeaderOne() {
        val backdrops = (1..12).map { TmdbImageDto("/b$it.jpg", it.toDouble()) } + TmdbImageDto("/header.jpg", 100.0) + TmdbImageDto("", 50.0)
        val details = TmdbMovieDto(backdropPath = "/header.jpg", images = TmdbImagesDto(backdrops)).toFilmDetails()

        assertThat(details.gallery.map { it.fullUrl }).isEqualTo((12 downTo 4).map { "https://image.tmdb.org/t/p/w1280/b$it.jpg" })
        assertThat(details.gallery.first().thumbUrl).isEqualTo("https://image.tmdb.org/t/p/w300/b12.jpg")
    }

    @Test
    fun noExtraBackdropsGiveNoGallery() {
        assertThat(TmdbMovieDto().toFilmDetails().gallery).isEmpty()
        assertThat(TmdbMovieDto(backdropPath = "/h.jpg", images = TmdbImagesDto(listOf(TmdbImageDto("/h.jpg", 5.0)))).toFilmDetails().gallery).isEmpty()
    }

    @Test
    fun descriptorsFollowTheCertification() {
        val withDescriptors = TmdbMovieDto(releaseDates = italianDates(date(3, "2024-01-01", "VM14", listOf("Violence", "Weapons", "Violence"))))
        assertThat(withDescriptors.toFilmDetails().rating).isEqualTo("14+ · Violence, Weapons")

        val without = TmdbMovieDto(releaseDates = italianDates(date(3, "2024-01-01", "T")))
        assertThat(without.toFilmDetails().rating).isEqualTo("Per tutti")
    }

    @Test
    fun descriptorsAreTakenFromTheEntryThatProvidesTheCertification() {
        val movie = TmdbMovieDto(
            releaseDates = italianDates(
                date(3, "2024-01-01", "", listOf("Ignored")),
                date(4, "2024-02-01", "6+", listOf("Fear"))
            )
        )

        assertThat(movie.toFilmDetails().rating).isEqualTo("6+ · Fear")
    }

    @Test
    fun descriptorsWithoutACertificationShowNothing() {
        val movie = TmdbMovieDto(releaseDates = italianDates(date(3, "2024-01-01", "", listOf("Violence"))))

        assertThat(movie.toFilmDetails().rating).isNull()
    }

    @Test
    fun blankCompaniesAreDroppedAndDuplicatesJoinedOnce() {
        val details = TmdbMovieDto(productionCompanies = listOf(TmdbCompanyDto("A24"), TmdbCompanyDto(" "), TmdbCompanyDto("A24"), TmdbCompanyDto("Blumhouse"))).toFilmDetails()

        assertThat(details.productionCompanies).isEqualTo("A24, Blumhouse")
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

        assertThat(details.posterUrl).isEqualTo("https://image.tmdb.org/t/p/w342/poster.jpg")
        assertThat(details.logoUrl).isEqualTo("https://image.tmdb.org/t/p/w500/it.png")
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

        assertThat(english.logoUrl).isEqualTo("https://image.tmdb.org/t/p/w500/en.png")
        assertThat(nothing.logoUrl).isNull()
    }

    @Test
    fun galleryKeepsItalianAndUntaggedBackdropsOnly() {
        val details = movie().copy(images = TmdbImagesDto(backdrops = listOf(
            TmdbImageDto(filePath = "/it.jpg", voteAverage = 5.0, language = "it"),
            TmdbImageDto(filePath = "/en.jpg", voteAverage = 9.0, language = "en"),
            TmdbImageDto(filePath = "/none.jpg", voteAverage = 7.0, language = null)
        ))).toFilmDetails()

        assertThat(details.gallery.map { it.fullUrl.substringAfter("w1280") }).isEqualTo(listOf("/none.jpg", "/it.jpg"))
    }
}
