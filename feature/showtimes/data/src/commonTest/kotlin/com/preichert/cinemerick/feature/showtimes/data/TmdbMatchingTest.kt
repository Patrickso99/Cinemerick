package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbMatchingTest {

    private val today = LocalDate(2026, 10, 14)

    private fun result(id: Int, title: String, release: String?, original: String = title, popularity: Double = 1.0) =
        TmdbSearchResultDto(id = id, title = title, originalTitle = original, releaseDate = release, popularity = popularity)

    @Test
    fun caseDoesNotBreakTheMatch() {
        val results = listOf(result(1, "Dune", "2026-09-01"))

        assertEquals(1, results.bestMatchFor("Dune", today)?.id)
        assertEquals(1, results.bestMatchFor("DUNE", today)?.id)
    }

    @Test
    fun accentsAndPunctuationAreIgnoredWhenComparing() {
        assertEquals(7, listOf(result(7, "Café", "2026-06-01")).bestMatchFor("Cafe", today)?.id)
        assertEquals(8, listOf(result(8, "Coyote vs. Acme", "2026-08-28")).bestMatchFor("Coyote Vs Acme", today)?.id)
    }

    @Test
    fun originalTitleMatchesWhenTheItalianOneDiffers() {
        val results = listOf(result(11, "Guerre stellari", "1977-05-25", original = "Star Wars"))

        assertEquals(11, results.bestMatchFor("Star Wars", today)?.id)
    }

    @Test
    fun reReleaseOfAClassicWithAUniqueTitleMatches() {
        val results = listOf(result(3, "Il gladiatore", "2000-05-01"))

        assertEquals(3, results.bestMatchFor("Il Gladiatore", today)?.id)
    }

    @Test
    fun theMostPopularOfSeveralSameTitleEntriesWins() {
        val results = listOf(
            result(1, "Oppenheimer", "2024-03-01", popularity = 0.2),
            result(2, "Oppenheimer", "2023-07-19", popularity = 80.0),
            result(3, "Oppenheimer", "1980-01-01", popularity = 1.5)
        )

        assertEquals(2, results.bestMatchFor("Oppenheimer", today)?.id)
    }

    @Test
    fun anObscureRecentHomonymDoesNotBeatTheRealFilm() {
        val results = listOf(
            result(1, "Joker", "2025-05-01", popularity = 0.1),
            result(2, "Joker", "2019-10-02", popularity = 60.0)
        )

        assertEquals(2, results.bestMatchFor("Joker", today)?.id)
    }

    @Test
    fun equalPopularityIsBrokenByTheMostRecentRelease() {
        val results = listOf(
            result(1, "Il grande Gatsby", "1974-03-29", popularity = 5.0),
            result(2, "Il grande Gatsby", "2013-05-10", popularity = 5.0),
            result(3, "Il grande Gatsby", "1949-07-12", popularity = 5.0)
        )

        assertEquals(2, results.bestMatchFor("Il grande Gatsby", today)?.id)
    }

    @Test
    fun aSequelOrSubtitleFilmIsNotTheSearchedFilm() {
        val sequel = result(1, "Star Wars: Episodio III - La vendetta dei Sith", "2005-05-19", original = "Star Wars: Episode III - Revenge of the Sith", popularity = 90.0)
        val original = result(2, "Guerre stellari", "1977-05-25", original = "Star Wars", popularity = 40.0)

        assertEquals(2, listOf(sequel, original).bestMatchFor("Star Wars", today)?.id)
        assertNull(listOf(sequel).bestMatchFor("Star Wars", today))
        assertNull(listOf(result(5, "Dune: Parte due", "2024-02-28")).bestMatchFor("Dune", today))
        assertNull(listOf(result(6, "Avatar: A New Era - Special Edition", "2025-12-01", popularity = 3.0)).bestMatchFor("Avatar", today))
    }

    @Test
    fun partialAndTruncatedTitlesAreNotAMatch() {
        val results = listOf(result(2, "Dune", "2021-09-15"))

        assertEquals(2, results.bestMatchFor("Dune", today)?.id)
        assertNull(results.bestMatchFor("Dun", today))
        assertNull(results.bestMatchFor("Dune Messiah", today))
    }

    @Test
    fun aSubtitleAddedByTheCinemaFallsBackToTheTitleBeforeIt() {
        val results = listOf(result(4, "Heart of the Beast", "2026-10-01"))

        assertEquals(4, results.bestMatchFor("Heart of the Beast - Nel profondo selvaggio", today)?.id)
    }

    @Test
    fun theFullTitleBeatsTheShortenedOneEvenWhenLessPopular() {
        val results = listOf(
            result(1, "Cars", "2006-06-09", popularity = 90.0),
            result(2, "Cars - Motori ruggenti", "2006-06-09", popularity = 2.0)
        )

        assertEquals(2, results.bestMatchFor("Cars - Motori ruggenti", today)?.id)
    }

    @Test
    fun filmDueBeyondSixMonthsIsIgnored() {
        val results = listOf(result(1, "Avatar 4", "2027-04-15", popularity = 99.0), result(2, "Avatar 4", "2026-12-18"))

        assertEquals(2, results.bestMatchFor("Avatar 4", today)?.id)
        assertNull(listOf(result(1, "Avatar 4", "2027-04-15")).bestMatchFor("Avatar 4", today))
        // Exactly six months ahead is still accepted.
        assertEquals(5, listOf(result(5, "Film", "2027-04-14")).bestMatchFor("Film", today)?.id)
    }

    @Test
    fun candidatesWithoutAUsableDateAreIgnored() {
        val results = listOf(result(1, "Senza data", ""), result(2, "Senza data", null), result(3, "Senza data", "non-una-data"))

        assertNull(results.bestMatchFor("Senza data", today))
    }

    @Test
    fun noCandidatesAndBlankTitlesGiveNoMatch() {
        assertNull(emptyList<TmdbSearchResultDto>().bestMatchFor("Dune", today))
        assertNull(listOf(result(1, "Dune", "2021-09-15")).bestMatchFor("   ", today))
        assertNull(listOf(result(1, "???", "2021-09-15")).bestMatchFor("!!!", today))
    }

    @Test
    fun nonLatinTitlesStillCompare() {
        val results = listOf(result(9, "千と千尋の神隠し", "2001-07-20", original = "千と千尋の神隠し"))

        assertEquals(9, results.bestMatchFor("千と千尋の神隠し", today)?.id)
    }
}
