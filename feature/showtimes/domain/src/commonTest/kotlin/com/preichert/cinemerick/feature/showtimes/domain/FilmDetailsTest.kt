package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        assertEquals(listOf(Chain.UCI, Chain.THE_SPACE, Chain.NOTORIOUS), chainPriority.take(3))
        assertEquals(Chain.entries.toSet(), chainPriority.toSet())
        assertTrue(Chain.UCI.priorityRank < Chain.THE_SPACE.priorityRank)
        assertTrue(Chain.THE_SPACE.priorityRank < Chain.NOTORIOUS.priorityRank)
        assertTrue(Chain.NOTORIOUS.priorityRank < Chain.ARCADIA.priorityRank)
    }

    @Test
    fun missingFieldFallsBackToNextChain() {
        val group = listOf(
            showing(Chain.THE_SPACE, details = FilmDetails(synopsis = "TS plot", director = "Rob Savage", cast = "TS cast")),
            showing(Chain.UCI, details = FilmDetails(synopsis = "UCI plot", genre = "Horror", rating = "14+"))
        ).groupByFilm().single()

        assertEquals(
            FilmDetails(synopsis = "UCI plot", genre = "Horror", director = "Rob Savage", cast = "TS cast", rating = "14+"),
            group.details
        )
    }

    @Test
    fun sameFieldFromSeveralChainsTakesTheBestChain() {
        val group = listOf(
            showing(Chain.NOTORIOUS, details = FilmDetails(synopsis = "Notorious plot")),
            showing(Chain.UCI, details = FilmDetails(synopsis = "UCI plot"))
        ).groupByFilm().single()

        assertEquals("UCI plot", group.details.synopsis)
    }

    @Test
    fun noDetailsGiveEmptyDetails() {
        val group = listOf(showing(Chain.UCI), showing(Chain.DON_BOSCO)).groupByFilm().single()

        assertTrue(group.details.isEmpty)
    }

    @Test
    fun posterFollowsChainPriority() {
        val group = listOf(
            showing(Chain.NOTORIOUS, poster = "notorious.jpg"),
            showing(Chain.THE_SPACE, poster = "thespace.jpg")
        ).groupByFilm().single()

        assertEquals("thespace.jpg", group.posterUrl)
    }

    @Test
    fun posterPrefersUciAndFallsBackToAnyChain() {
        val withUci = listOf(showing(Chain.THE_SPACE, poster = "ts.jpg"), showing(Chain.UCI, poster = "uci.jpg")).groupByFilm().single()
        val withoutUci = listOf(showing(Chain.CRISTALLO, poster = "cristallo.jpg"), showing(Chain.UCI)).groupByFilm().single()

        assertEquals("uci.jpg", withUci.posterUrl)
        assertEquals("cristallo.jpg", withoutUci.posterUrl)
    }

    @Test
    fun detailsSourcesKeepOnePageForEachChainBestChainFirst() {
        val group = listOf(
            showing(Chain.CRISTALLO, detailsUrl = "https://cristallo/movie/a"),
            showing(Chain.NOTORIOUS, time = "20:00", detailsUrl = "https://notorious/1"),
            showing(Chain.NOTORIOUS, time = "22:00", detailsUrl = "https://notorious/2"),
            showing(Chain.UCI)
        ).groupByFilm().single()

        assertEquals(
            listOf(
                DetailsSource(Chain.NOTORIOUS, "https://notorious/1"),
                DetailsSource(Chain.CRISTALLO, "https://cristallo/movie/a")
            ),
            group.detailsSources
        )
    }

    @Test
    fun tmdbOnlyFieldsMakeDetailsNonEmptyButDoNotCountForCompleteness() {
        assertTrue(!FilmDetails(runtimeMinutes = 121).isEmpty)
        assertTrue(!FilmDetails(year = 1977).isEmpty)
        assertTrue(!FilmDetails(originalTitle = "Star Wars").isEmpty)
        assertTrue(!FilmDetails(score = FilmScore(8.2, 100)).isEmpty)

        val chainFieldsOnly = FilmDetails(
            synopsis = "s", genre = "g", director = "d", cast = "c", rating = "r", trailerUrl = "t"
        )
        assertTrue(chainFieldsOnly.isComplete)
        assertTrue(!FilmDetails(runtimeMinutes = 121, year = 1977, originalTitle = "o", score = FilmScore(8.2, 1)).isComplete)
    }

    @Test
    fun withFallbackCarriesTmdbOnlyFields() {
        val merged = FilmDetails(runtimeMinutes = 100).withFallback(
            FilmDetails(runtimeMinutes = 200, year = 1977, originalTitle = "Star Wars", score = FilmScore(8.2, 10))
        )

        assertEquals(FilmDetails(runtimeMinutes = 100, year = 1977, originalTitle = "Star Wars", score = FilmScore(8.2, 10)), merged)
    }

    @Test
    fun withFallbackKeepsOwnFieldsAndFillsGaps() {
        val merged = FilmDetails(synopsis = "own").withFallback(FilmDetails(synopsis = "other", genre = "Drama"))

        assertEquals(FilmDetails(synopsis = "own", genre = "Drama"), merged)
        assertNull(merged.trailerUrl)
    }

    @Test
    fun enrichmentFieldsMakeDetailsNonEmptyButDoNotCountForCompleteness() {
        val person = FilmPerson(1, "Mark Hamill", "Luke Skywalker", "https://img/p.jpg")
        assertTrue(!FilmDetails(tagline = "A long time ago").isEmpty)
        assertTrue(!FilmDetails(backdropUrl = "https://img/b.jpg").isEmpty)
        assertTrue(!FilmDetails(saga = "Star Wars Collection").isEmpty)
        assertTrue(!FilmDetails(originalLanguage = "en").isEmpty)
        assertTrue(!FilmDetails(countries = "United States").isEmpty)
        assertTrue(!FilmDetails(castMembers = listOf(person)).isEmpty)
        assertTrue(!FilmDetails(directors = listOf(person)).isEmpty)

        val chainFieldsOnly = FilmDetails(synopsis = "s", genre = "g", director = "d", cast = "c", rating = "r", trailerUrl = "t")
        assertTrue(chainFieldsOnly.copy(tagline = "t", saga = "s", castMembers = listOf(person)).isComplete)
        assertTrue(!FilmDetails(tagline = "t", saga = "s", castMembers = listOf(person)).isComplete)
    }

    @Test
    fun withFallbackKeepsEnrichmentFields() {
        val person = FilmPerson(1, "Mark Hamill")
        val other = FilmPerson(2, "Carrie Fisher")
        val merged = FilmDetails(tagline = "own", castMembers = listOf(person)).withFallback(
            FilmDetails(tagline = "other", saga = "Saga", castMembers = listOf(other), directors = listOf(other))
        )

        assertEquals(
            FilmDetails(tagline = "own", saga = "Saga", castMembers = listOf(person), directors = listOf(other)),
            merged
        )
    }

    @Test
    fun factsAndGalleryMakeDetailsNonEmptyButDoNotCountForCompleteness() {
        assertTrue(!FilmDetails(productionCompanies = "Legendary").isEmpty)
        assertTrue(!FilmDetails(budget = 1L).isEmpty)
        assertTrue(!FilmDetails(revenue = 1L).isEmpty)
        assertTrue(!FilmDetails(releaseDate = LocalDate(2024, 2, 28)).isEmpty)
        assertTrue(!FilmDetails(gallery = listOf(FilmImage("t", "f"))).isEmpty)

        val chainFieldsOnly = FilmDetails(synopsis = "s", genre = "g", director = "d", cast = "c", rating = "r", trailerUrl = "t")
        assertTrue(chainFieldsOnly.copy(budget = 1L, gallery = listOf(FilmImage("t", "f"))).isComplete)
        assertTrue(!FilmDetails(budget = 1L, revenue = 1L, productionCompanies = "x").isComplete)
    }

    @Test
    fun withFallbackKeepsFactsAndGallery() {
        val own = FilmImage("own-t", "own-f")
        val merged = FilmDetails(budget = 10L, gallery = listOf(own)).withFallback(
            FilmDetails(budget = 99L, revenue = 20L, productionCompanies = "A", releaseDate = LocalDate(2024, 1, 1), gallery = listOf(FilmImage("x", "y")))
        )

        assertEquals(
            FilmDetails(budget = 10L, revenue = 20L, productionCompanies = "A", releaseDate = LocalDate(2024, 1, 1), gallery = listOf(own)),
            merged
        )
    }
}
