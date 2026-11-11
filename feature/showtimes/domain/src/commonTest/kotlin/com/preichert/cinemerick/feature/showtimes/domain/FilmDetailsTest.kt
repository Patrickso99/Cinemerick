package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue

class FilmDetailsTest {

    private val day = LocalDate(2026, 10, 13)

    private fun venue(chain: Chain) = Venue(chain, chain.name.lowercase(), chain.name)

    private fun showing(
        chain: Chain,
        time: String = "21:00",
        poster: String? = null,
        details: FilmDetails? = null,
        detailsUrl: String? = null
    ) = Showing("Mammina", day, LocalTime.parse(time), venue(chain), posterUrl = poster, details = details, detailsUrl = detailsUrl)

    @Test
    fun priorityListsUciTheSpaceNotoriousFirstAndRanksEveryChain() {
        assertThat(chainPriority.take(3)).isEqualTo(listOf(Chain.UCI, Chain.THE_SPACE, Chain.NOTORIOUS))
        assertThat(chainPriority.toSet()).isEqualTo(Chain.entries.toSet())
        assertThat(Chain.UCI.priorityRank < Chain.THE_SPACE.priorityRank).isTrue()
        assertThat(Chain.THE_SPACE.priorityRank < Chain.NOTORIOUS.priorityRank).isTrue()
        assertThat(Chain.NOTORIOUS.priorityRank < Chain.ARCADIA.priorityRank).isTrue()
    }

    @Test
    fun missingFieldFallsBackToNextChain() {
        val group = listOf(
            showing(Chain.THE_SPACE, details = FilmDetails(synopsis = "TS plot", director = "Rob Savage", cast = "TS cast")),
            showing(Chain.UCI, details = FilmDetails(synopsis = "UCI plot", genre = "Horror", rating = "14+"))
        ).groupByFilm().single()

        assertThat(group.details).isEqualTo(FilmDetails(synopsis = "UCI plot", genre = "Horror", director = "Rob Savage", cast = "TS cast", rating = "14+"))
    }

    @Test
    fun sameFieldFromSeveralChainsTakesTheBestChain() {
        val group = listOf(
            showing(Chain.NOTORIOUS, details = FilmDetails(synopsis = "Notorious plot")),
            showing(Chain.UCI, details = FilmDetails(synopsis = "UCI plot"))
        ).groupByFilm().single()

        assertThat(group.details.synopsis).isEqualTo("UCI plot")
    }

    @Test
    fun noDetailsGiveEmptyDetails() {
        val group = listOf(showing(Chain.UCI), showing(Chain.DON_BOSCO)).groupByFilm().single()

        assertThat(group.details.isEmpty).isTrue()
    }

    @Test
    fun posterFollowsChainPriority() {
        val group = listOf(
            showing(Chain.NOTORIOUS, poster = "notorious.jpg"),
            showing(Chain.THE_SPACE, poster = "thespace.jpg")
        ).groupByFilm().single()

        assertThat(group.posterUrl).isEqualTo("thespace.jpg")
    }

    @Test
    fun posterPrefersUciAndFallsBackToAnyChain() {
        val withUci = listOf(showing(Chain.THE_SPACE, poster = "ts.jpg"), showing(Chain.UCI, poster = "uci.jpg")).groupByFilm().single()
        val withoutUci = listOf(showing(Chain.CRISTALLO, poster = "cristallo.jpg"), showing(Chain.UCI)).groupByFilm().single()

        assertThat(withUci.posterUrl).isEqualTo("uci.jpg")
        assertThat(withoutUci.posterUrl).isEqualTo("cristallo.jpg")
    }

    @Test
    fun detailsSourcesKeepOnePageForEachChainBestChainFirst() {
        val group = listOf(
            showing(Chain.CRISTALLO, detailsUrl = "https://cristallo/movie/a"),
            showing(Chain.NOTORIOUS, time = "20:00", detailsUrl = "https://notorious/1"),
            showing(Chain.NOTORIOUS, time = "22:00", detailsUrl = "https://notorious/2"),
            showing(Chain.UCI)
        ).groupByFilm().single()

        assertThat(group.detailsSources).isEqualTo(listOf(
                DetailsSource(Chain.NOTORIOUS, "https://notorious/1"),
                DetailsSource(Chain.CRISTALLO, "https://cristallo/movie/a")
            ))
    }

    @Test
    fun tmdbOnlyFieldsMakeDetailsNonEmptyButDoNotCountForCompleteness() {
        assertThat(!FilmDetails(runtimeMinutes = 121).isEmpty).isTrue()
        assertThat(!FilmDetails(year = 1977).isEmpty).isTrue()
        assertThat(!FilmDetails(originalTitle = "Star Wars").isEmpty).isTrue()
        assertThat(!FilmDetails(score = FilmScore(8.2, 100)).isEmpty).isTrue()

        val chainFieldsOnly = FilmDetails(
            synopsis = "s", genre = "g", director = "d", cast = "c", rating = "r", trailerUrl = "t"
        )
        assertThat(chainFieldsOnly.isComplete).isTrue()
        assertThat(!FilmDetails(runtimeMinutes = 121, year = 1977, originalTitle = "o", score = FilmScore(8.2, 1)).isComplete).isTrue()
    }

    @Test
    fun withFallbackCarriesTmdbOnlyFields() {
        val merged = FilmDetails(runtimeMinutes = 100).withFallback(
            FilmDetails(runtimeMinutes = 200, year = 1977, originalTitle = "Star Wars", score = FilmScore(8.2, 10))
        )

        assertThat(merged).isEqualTo(FilmDetails(runtimeMinutes = 100, year = 1977, originalTitle = "Star Wars", score = FilmScore(8.2, 10)))
    }

    @Test
    fun withFallbackKeepsOwnFieldsAndFillsGaps() {
        val merged = FilmDetails(synopsis = "own").withFallback(FilmDetails(synopsis = "other", genre = "Drama"))

        assertThat(merged).isEqualTo(FilmDetails(synopsis = "own", genre = "Drama"))
        assertThat(merged.trailerUrl).isNull()
    }

    @Test
    fun enrichmentFieldsMakeDetailsNonEmptyButDoNotCountForCompleteness() {
        val person = FilmPerson(1, "Mark Hamill", "Luke Skywalker", "https://img/p.jpg")
        assertThat(!FilmDetails(tagline = "A long time ago").isEmpty).isTrue()
        assertThat(!FilmDetails(backdropUrl = "https://img/b.jpg").isEmpty).isTrue()
        assertThat(!FilmDetails(saga = "Star Wars Collection").isEmpty).isTrue()
        assertThat(!FilmDetails(originalLanguage = "en").isEmpty).isTrue()
        assertThat(!FilmDetails(countries = "United States").isEmpty).isTrue()
        assertThat(!FilmDetails(castMembers = listOf(person)).isEmpty).isTrue()
        assertThat(!FilmDetails(directors = listOf(person)).isEmpty).isTrue()

        val chainFieldsOnly = FilmDetails(synopsis = "s", genre = "g", director = "d", cast = "c", rating = "r", trailerUrl = "t")
        assertThat(chainFieldsOnly.copy(tagline = "t", saga = "s", castMembers = listOf(person)).isComplete).isTrue()
        assertThat(!FilmDetails(tagline = "t", saga = "s", castMembers = listOf(person)).isComplete).isTrue()
    }

    @Test
    fun withFallbackKeepsEnrichmentFields() {
        val person = FilmPerson(1, "Mark Hamill")
        val other = FilmPerson(2, "Carrie Fisher")
        val merged = FilmDetails(tagline = "own", castMembers = listOf(person)).withFallback(
            FilmDetails(tagline = "other", saga = "Saga", castMembers = listOf(other), directors = listOf(other))
        )

        assertThat(merged).isEqualTo(FilmDetails(tagline = "own", saga = "Saga", castMembers = listOf(person), directors = listOf(other)))
    }

    @Test
    fun factsAndGalleryMakeDetailsNonEmptyButDoNotCountForCompleteness() {
        assertThat(!FilmDetails(productionCompanies = "Legendary").isEmpty).isTrue()
        assertThat(!FilmDetails(budget = 1L).isEmpty).isTrue()
        assertThat(!FilmDetails(revenue = 1L).isEmpty).isTrue()
        assertThat(!FilmDetails(releaseDate = LocalDate(2024, 2, 28)).isEmpty).isTrue()
        assertThat(!FilmDetails(gallery = listOf(FilmImage("t", "f"))).isEmpty).isTrue()

        val chainFieldsOnly = FilmDetails(synopsis = "s", genre = "g", director = "d", cast = "c", rating = "r", trailerUrl = "t")
        assertThat(chainFieldsOnly.copy(budget = 1L, gallery = listOf(FilmImage("t", "f"))).isComplete).isTrue()
        assertThat(!FilmDetails(budget = 1L, revenue = 1L, productionCompanies = "x").isComplete).isTrue()
    }

    @Test
    fun withFallbackKeepsFactsAndGallery() {
        val own = FilmImage("own-t", "own-f")
        val merged = FilmDetails(budget = 10L, gallery = listOf(own)).withFallback(
            FilmDetails(budget = 99L, revenue = 20L, productionCompanies = "A", releaseDate = LocalDate(2024, 1, 1), gallery = listOf(FilmImage("x", "y")))
        )

        assertThat(merged).isEqualTo(FilmDetails(budget = 10L, revenue = 20L, productionCompanies = "A", releaseDate = LocalDate(2024, 1, 1), gallery = listOf(own)))
    }
}
