package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ShowtimesLogicTest {

    private val thursday = LocalDate(2026, 10, 1)
    private val friday = LocalDate(2026, 10, 2)
    private val farPast = LocalDateTime(2026, 9, 1, 0, 0)

    private val venueUci = Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", region = "Veneto", webUrl = "https://ucicinemas.it")
    private val venueTheSpace = Venue(Chain.THE_SPACE, "1009", "Silea", region = "Veneto", webUrl = "https://thespace.it")
    private val venueNotorious = Venue(Chain.NOTORIOUS, "ferrara", "Ferrara", region = "Emilia-Romagna", webUrl = "https://notoriouscinemas.it")

    private fun showing(title: String, day: LocalDate, time: String, venue: Venue) =
        Showing(title, day, LocalTime.parse(time), venue)

    private fun showing(title: String, format: String?) =
        Showing(title, thursday, LocalTime.parse("21:00"), venueUci, format)

    @Test
    fun fetchIsNeededOnlyForNewDaysOrCinemas() {
        val days = setOf(thursday, friday)
        val venues = setOf(venueUci, venueTheSpace)

        assertEquals(false, needsFetch(days, venues, setOf(thursday), setOf(venueUci)))
        assertEquals(false, needsFetch(days, venues, days, venues))
        assertEquals(true, needsFetch(days, venues, days, venues + venueNotorious))
        assertEquals(true, needsFetch(days, venues, days + LocalDate(2026, 10, 3), venues))
    }

    @Test
    fun tagsAreParsedFromFormat() {
        assertEquals(setOf("2D", "INFINITY VISION", "VO"), showing("A", "2D · INFINITY VISION · VO").tags)
        assertEquals(setOf("2D"), showing("A", null).tags)
        assertEquals(setOf("VO", "2D"), showing("A", "VO").tags)
        assertEquals(setOf("3D", "VO"), showing("A", "3D · VO").tags)
    }

    @Test
    fun displayFormatAddsImpliedDimension() {
        assertEquals("2D", showing("A", null).displayFormat)
        assertEquals("2D · VO", showing("A", "VO").displayFormat)
        assertEquals("3D · VO", showing("A", "3D · VO").displayFormat)
    }

    @Test
    fun availableTagsAreSortedAndDistinct() {
        val tags = listOf(showing("A", "3D · VO"), showing("B", "2D"), showing("C", null), showing("D", "3D"))
            .availableTags()

        assertEquals(listOf("2D", "3D", "VO"), tags)
    }

    @Test
    fun hiddenTagsDropMatchingShowingsAndUntaggedCountAs2D() {
        val showings = listOf(showing("A", "3D · VO"), showing("B", "2D"), showing("C", null))
        val ranges = listOf(DayRange(thursday))

        val result = showings.filterShowings(ranges, emptyList(), farPast, hiddenTags = setOf("3D", "VO"))
        val no2d = showings.filterShowings(ranges, emptyList(), farPast, hiddenTags = setOf("2D"))

        assertEquals(listOf("B", "C"), result.map { it.title })
        assertEquals(listOf("A"), no2d.map { it.title })
        assertEquals(3, showings.filterShowings(ranges, emptyList(), farPast).size)
    }

    @Test
    fun sameFilmFromBothCinemasIsGroupedWithShortestTitleAsHeader() {
        val groups = listOf(
            showing("HEART OF THE BEAST - NEL PROFONDO SELVAGGIO", thursday, "21:40", venueTheSpace),
            showing("Heart of the Beast", thursday, "21:40", venueUci),
            showing("Naza", friday, "21:40", venueTheSpace),
            showing("Naza C.A.", friday, "19:40", venueUci)
        ).groupByFilm()

        // Group titles are the shortest originals
        assertEquals(listOf("Heart of the Beast", "Naza"), groups.map { it.title })
        // Original titles are preserved in showings (not transformed)
        assertEquals(
            listOf("Heart of the Beast", "HEART OF THE BEAST - NEL PROFONDO SELVAGGIO"),
            groups.first().showings.map { it.title }
        )
        assertEquals(
            listOf("Naza C.A.", "Naza"),
            groups.last().showings.map { it.title }
        )
        // Cinema and time ordering is preserved
        assertEquals(
            listOf(venueUci, venueTheSpace),
            groups.first().showings.map { it.venue }
        )
        assertEquals(
            listOf(LocalTime.parse("19:40"), LocalTime.parse("21:40")),
            groups.last().showings.map { it.time }
        )
    }

    @Test
    fun pollTextUsesGroupTitleForAllCinemas() {
        val text = listOf(
            showing("Heart Of The Beast - Nel Profondo Selvaggio", thursday, "21:40", venueTheSpace),
            showing("Heart of the Beast", thursday, "21:40", venueUci)
        ).groupByFilm().toPollText()

        assertEquals(
            "Heart of the Beast (Giovedì - 21:40 - Marcon)\nHeart of the Beast (Giovedì - 21:40 - Silea)",
            text
        )
    }

    private fun validTime(text: String): LocalTime? =
        (parseTimeInput(text) as ParsedTime.Valid).value

    @Test
    fun parseTimeInputAcceptsHourOnlyAndFullTimes() {
        assertEquals(null, validTime(""))
        assertEquals(null, validTime("   "))
        assertEquals(LocalTime(21, 0), validTime("21"))
        assertEquals(LocalTime(9, 0), validTime("9"))
        assertEquals(LocalTime(0, 0), validTime("0"))
        assertEquals(LocalTime(9, 30), validTime("9:30"))
        assertEquals(LocalTime(21, 15), validTime("21:15"))
        assertEquals(LocalTime(21, 15), validTime(" 21:15 "))
    }

    @Test
    fun parseTimeInputMapsTwentyFourToLastMinuteOfDay() {
        assertEquals(LocalTime(23, 59), validTime("24"))
        assertEquals(LocalTime(23, 59), validTime("24:00"))
    }

    @Test
    fun parseTimeInputRejectsInvalidValues() {
        listOf("25", "24:30", "abc", "21:60", "99", "-1", "21:", ":30", "2:3:4").forEach {
            assertEquals(ParsedTime.Invalid, parseTimeInput(it), "'$it' should be invalid")
        }
    }

    @Test
    fun differentFormatsOfSameFilmAreNotGroupedTogether() {
        val groups = listOf(
            showing("Avatar 2D", thursday, "21:00", venueTheSpace),
            showing("Avatar 3D", thursday, "19:00", venueUci),
            showing("Avatar XL", friday, "21:30", venueTheSpace)
        ).groupByFilm()

        // Each format variant has a different filmKey, so they are separate groups
        assertEquals(3, groups.size)
        assertEquals(
            listOf("Avatar 2D", "Avatar 3D", "Avatar XL"),
            groups.map { it.title }
        )
    }

    @Test
    fun perDayRangeIsApplied() {
        val showings = listOf(
            showing("Digger", thursday, "19:00", venueTheSpace),
            showing("Digger", thursday, "21:30", venueUci),
            showing("Digger", friday, "21:30", venueUci),
            showing("Digger", friday, "23:15", venueUci)
        )
        val ranges = listOf(
            DayRange(thursday, min = LocalTime.parse("21:00"), max = LocalTime.parse("22:30")),
            DayRange(friday, min = LocalTime.parse("22:00"))
        )

        val result = showings.filterShowings(ranges, emptyList(), farPast)

        assertEquals(
            listOf(
                showing("Digger", thursday, "21:30", venueUci),
                showing("Digger", friday, "23:15", venueUci)
            ),
            result
        )
    }

    @Test
    fun pastShowingsOfTodayAreDropped() {
        val now = LocalDateTime(2026, 10, 1, 20, 0)
        val showings = listOf(
            showing("Digger", thursday, "19:40", venueTheSpace),
            showing("Digger", thursday, "20:00", venueTheSpace),
            showing("Digger", thursday, "21:00", venueTheSpace)
        )

        val result = showings.filterShowings(listOf(DayRange(thursday)), emptyList(), now)

        assertEquals(listOf(LocalTime.parse("21:00")), result.map { it.time })
    }

    @Test
    fun pollLineMatchesPythonFormat() {
        val line = showing("Digger", thursday, "21:00", venueTheSpace).toPollLine()

        assertEquals("Digger (Giovedì - 21:00 - Silea)", line)
    }

    @Test
    fun pollLineAppendsFormatWhenPresent() {
        val day = LocalDate(2026, 10, 1)
        val xl = Showing("Avengers: Endgame Extra", day, LocalTime.parse("20:15"), venueUci, "XL")
        assertEquals("Avengers: Endgame Extra (Giovedì - 20:15 - Marcon - XL)", xl.toPollLine())
        assertEquals("Avengers: Endgame Extra (Giovedì - 20:15 - Marcon)", xl.copy(format = null).toPollLine())
    }

    @Test
    fun sameTimeInDifferentFormatsIsNotMerged() {
        val day = LocalDate(2026, 10, 1)
        val xl = Showing("Avengers", day, LocalTime.parse("20:15"), venueUci, "XL")
        val twoD = xl.copy(format = "2D")
        assertEquals(2, listOf(xl, twoD).distinct().size)
    }

    @Test
    fun cleanTitleConvertsAllCapsToTitleCaseAndKeepsOthers() {
        assertEquals("Avengers: Endgame Extra", cleanTitle("AVENGERS: ENDGAME EXTRA"))
        assertEquals("Avengers: Endgame Extra", cleanTitle("Avengers:  Endgame Extra "))
        assertEquals("Linkin Park: Unshatter C.A.", cleanTitle("Linkin Park: Unshatter C.A."))
    }

    @Test
    fun filmKeyHandlesPunctuationAndAccentVariantsCorrectly() {
        // Periods: "Vs." should match "Vs"
        assertEquals(filmKey("Coyote Vs Acme"), filmKey("Coyote Vs. Acme"))

        // Apostrophes and backticks: different apostrophe variants should match
        assertEquals(filmKey("L'Isola Dei Ricordi"), filmKey("L`Isola Dei Ricordi"))

        // Multiple punctuation: various punctuation should be ignored
        assertEquals(filmKey("Toy Story"), filmKey("Toy Story."))

        // Commas and other punctuation
        assertEquals(filmKey("Hello, World!"), filmKey("Hello World"))

        // Mixed: accents with punctuation
        assertEquals(filmKey("L'Été"), filmKey("L'Ete"))
        assertEquals(filmKey("L'Été"), filmKey("LÉte"))
    }

    @Test
    fun sameFilmWithPunctuationVariantsIsGrouped() {
        val groups = listOf(
            showing("Coyote Vs Acme", thursday, "19:00", venueTheSpace),
            showing("Coyote Vs. Acme", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertEquals(1, groups.size)
        assertEquals("Coyote Vs Acme", groups.first().title)
        assertEquals(2, groups.first().showings.size)
    }

    @Test
    fun filmKeyHandlesColonAndDashSubtitleSeparators() {
        // Colon and dash separators should produce the same key
        assertEquals(filmKey("Cars - Motori Ruggenti"), filmKey("Cars: Motori Ruggenti - 20 Anni"))
    }

    @Test
    fun carsMovieVariantsAreGroupedTogether() {
        val groups = listOf(
            showing("Cars - Motori Ruggenti", thursday, "17:00", venueTheSpace),
            showing("Cars: Motori Ruggenti - 20 Anni", thursday, "19:00", venueUci)
        ).groupByFilm()

        assertEquals(1, groups.size)
        assertEquals("Cars - Motori Ruggenti", groups.first().title)
        assertEquals(2, groups.first().showings.size)
    }

    @Test
    fun filmKeyHandlesColonWithLanguageSuffix() {
        // Colon with language suffix should keep both title and subtitle
        assertEquals(filmKey("Linkin Park: Unshatter"), filmKey("LINKIN PARK UNSHATTER - LINGUA ORIGINALE"))
        // Both should normalize to "linkin park unshatter"
        assertEquals("linkin park unshatter", filmKey("Linkin Park: Unshatter"))
        assertEquals("linkin park unshatter", filmKey("LINKIN PARK UNSHATTER - LINGUA ORIGINALE"))
    }

    @Test
    fun linkinParkVariantsAreGroupedTogether() {
        val groups = listOf(
            showing("Linkin Park: Unshatter", thursday, "19:00", venueTheSpace),
            showing("LINKIN PARK UNSHATTER - LINGUA ORIGINALE", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertEquals(1, groups.size)
        assertEquals("Linkin Park: Unshatter", groups.first().title)
        assertEquals(2, groups.first().showings.size)
    }

    @Test
    fun filmKeyStripsTrailingFormatSuffixInParentheses() {
        // Format suffixes in trailing parentheses should be stripped
        assertEquals(filmKey("Avengers: Endgame Extra"), filmKey("Avengers: Endgame Extra (Infinity Vision)"))
        assertEquals(filmKey("Avatar"), filmKey("Avatar (IMAX)"))
        assertEquals("avengers endgame extra", filmKey("Avengers: Endgame Extra (Infinity Vision)"))
    }

    @Test
    fun avengersAndAvengersWithInfinityVisionAreGroupedTogether() {
        val groups = listOf(
            showing("Avengers: Endgame Extra", thursday, "19:00", venueTheSpace),
            showing("Avengers: Endgame Extra (Infinity Vision)", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertEquals(1, groups.size)
        assertEquals("Avengers: Endgame Extra", groups.first().title)
        assertEquals(2, groups.first().showings.size)
    }

    @Test
    fun unrelatedParenthesesAreNotStripped() {
        // Parentheses without format keywords should not be stripped
        assertEquals(filmKey("Film (Parte 2)"), filmKey("Film (Parte 2)"))
        assertEquals(false, filmKey("Avatar 2D") == filmKey("Avatar 3D"))
        assertEquals(false, filmKey("Avatar XL") == filmKey("Avatar"))
    }

    @Test
    fun avatarVariantsRemainSeparate() {
        // Avatar in different formats should NOT be grouped together
        val groups = listOf(
            showing("Avatar 2D", thursday, "19:00", venueTheSpace),
            showing("Avatar 3D", thursday, "19:00", venueUci),
            showing("Avatar XL", thursday, "19:00", venueNotorious)
        ).groupByFilm()

        assertEquals(3, groups.size)
    }
}
