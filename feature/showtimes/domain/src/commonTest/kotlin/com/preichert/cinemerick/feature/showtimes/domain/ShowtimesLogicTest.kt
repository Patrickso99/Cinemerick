package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue

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

        assertThat(needsFetch(days, venues, setOf(thursday), setOf(venueUci))).isFalse()
        assertThat(needsFetch(days, venues, days, venues)).isFalse()
        assertThat(needsFetch(days, venues, days, venues + venueNotorious)).isTrue()
        assertThat(needsFetch(days, venues, days + LocalDate(2026, 10, 3), venues)).isTrue()
    }

    @Test
    fun tagsAreParsedFromFormat() {
        assertThat(showing("A", "2D · INFINITY VISION · VO").tags).isEqualTo(setOf("2D", "INFINITY VISION", "VO"))
        assertThat(showing("A", null).tags).isEqualTo(setOf("2D"))
        assertThat(showing("A", "VO").tags).isEqualTo(setOf("VO", "2D"))
        assertThat(showing("A", "3D · VO").tags).isEqualTo(setOf("3D", "VO"))
    }

    @Test
    fun displayFormatAddsImpliedDimension() {
        assertThat(showing("A", null).displayFormat).isEqualTo("2D")
        assertThat(showing("A", "VO").displayFormat).isEqualTo("2D · VO")
        assertThat(showing("A", "3D · VO").displayFormat).isEqualTo("3D · VO")
    }

    @Test
    fun availableTagsAreSortedAndDistinct() {
        val tags = listOf(showing("A", "3D · VO"), showing("B", "2D"), showing("C", null), showing("D", "3D"))
            .availableTags()

        assertThat(tags).isEqualTo(listOf("2D", "3D", "VO"))
    }

    @Test
    fun hiddenTagsDropMatchingShowingsAndUntaggedCountAs2D() {
        val showings = listOf(showing("A", "3D · VO"), showing("B", "2D"), showing("C", null))
        val ranges = listOf(DayRange(thursday))

        val result = showings.filterShowings(ranges, emptyList(), farPast, hiddenTags = setOf("3D", "VO"))
        val no2d = showings.filterShowings(ranges, emptyList(), farPast, hiddenTags = setOf("2D"))

        assertThat(result.map { it.title }).isEqualTo(listOf("B", "C"))
        assertThat(no2d.map { it.title }).isEqualTo(listOf("A"))
        assertThat(showings.filterShowings(ranges, emptyList(), farPast).size).isEqualTo(3)
    }

    @Test
    fun sameFilmFromBothCinemasIsGroupedWithShortestTitleAsHeader() {
        val groups = listOf(
            showing("HEART OF THE BEAST", thursday, "21:40", venueTheSpace),
            showing("Heart of the Beast", thursday, "21:40", venueUci),
            showing("Naza", friday, "21:40", venueTheSpace),
            showing("Naza C.A.", friday, "19:40", venueUci)
        ).groupByFilm()

        // Group titles are the shortest originals
        assertThat(groups.map { it.title }).isEqualTo(listOf("HEART OF THE BEAST", "Naza"))
        // Original titles are preserved in showings (not transformed); at the same time they are ordered by venue name
        assertThat(groups.first().showings.map { it.title }).isEqualTo(listOf("HEART OF THE BEAST", "Heart of the Beast"))
        assertThat(groups.last().showings.map { it.title }).isEqualTo(listOf("Naza C.A.", "Naza"))
        // Cinema and time ordering is preserved: same time, so by venue name ("Silea" before "UCI Luxe Marcon")
        assertThat(groups.first().showings.map { it.venue }).isEqualTo(listOf(venueTheSpace, venueUci))
        assertThat(groups.last().showings.map { it.time }).isEqualTo(listOf(LocalTime.parse("19:40"), LocalTime.parse("21:40")))
    }

    @Test
    fun pollTextSkipsSoldOutShowings() {
        val text = listOf(
            showing("Digger", thursday, "19:00", venueTheSpace).copy(soldOut = true),
            showing("Digger", thursday, "21:00", venueTheSpace)
        ).groupByFilm().toPollText()

        assertThat(text).isEqualTo("Digger (Giovedì - 21:00 - Silea)")
    }

    @Test
    fun pollTextIsEmptyWhenEveryShowingIsSoldOut() {
        val text = listOf(showing("Digger", thursday, "19:00", venueTheSpace).copy(soldOut = true)).groupByFilm().toPollText()

        assertThat(text).isEqualTo("")
    }

    @Test
    fun pollTextUsesGroupTitleForAllCinemas() {
        val text = listOf(
            showing("Heart Of The Beast", thursday, "21:40", venueTheSpace),
            showing("Heart of the Beast", thursday, "21:40", venueUci)
        ).groupByFilm().toPollText()

        assertThat(text).isEqualTo("Heart Of The Beast (Giovedì - 21:40 - Silea)\nHeart Of The Beast (Giovedì - 21:40 - UCI Luxe Marcon)")
    }

    private fun validTime(text: String): LocalTime? =
        (parseTimeInput(text) as ParsedTime.Valid).value

    @Test
    fun parseTimeInputAcceptsHourOnlyAndFullTimes() {
        assertThat(validTime("")).isNull()
        assertThat(validTime("   ")).isNull()
        assertThat(validTime("21")).isEqualTo(LocalTime(21, 0))
        assertThat(validTime("9")).isEqualTo(LocalTime(9, 0))
        assertThat(validTime("0")).isEqualTo(LocalTime(0, 0))
        assertThat(validTime("9:30")).isEqualTo(LocalTime(9, 30))
        assertThat(validTime("21:15")).isEqualTo(LocalTime(21, 15))
        assertThat(validTime(" 21:15 ")).isEqualTo(LocalTime(21, 15))
    }

    @Test
    fun parseTimeInputMapsTwentyFourToLastMinuteOfDay() {
        assertThat(validTime("24")).isEqualTo(LocalTime(23, 59))
        assertThat(validTime("24:00")).isEqualTo(LocalTime(23, 59))
    }

    @Test
    fun parseTimeInputRejectsInvalidValues() {
        listOf("25", "24:30", "abc", "21:60", "99", "-1", "21:", ":30", "2:3:4").forEach {
            assertThat(parseTimeInput(it), name = "input=\"$it\"").isEqualTo(ParsedTime.Invalid)
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
        assertThat(groups.size).isEqualTo(3)
        assertThat(groups.map { it.title }).isEqualTo(listOf("Avatar 2D", "Avatar 3D", "Avatar XL"))
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

        assertThat(result).isEqualTo(listOf(
                showing("Digger", thursday, "21:30", venueUci),
                showing("Digger", friday, "23:15", venueUci)
            ))
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

        assertThat(result.map { it.time }).isEqualTo(listOf(LocalTime.parse("21:00")))
    }

    @Test
    fun pollLineMatchesPythonFormat() {
        val line = showing("Digger", thursday, "21:00", venueTheSpace).toPollLine()

        assertThat(line).isEqualTo("Digger (Giovedì - 21:00 - Silea)")
    }

    @Test
    fun pollLineAppendsFormatWhenPresent() {
        val day = LocalDate(2026, 10, 1)
        val xl = Showing("Avengers: Endgame Extra", day, LocalTime.parse("20:15"), venueUci, "XL")
        assertThat(xl.toPollLine()).isEqualTo("Avengers: Endgame Extra (Giovedì - 20:15 - UCI Luxe Marcon - XL)")
        assertThat(xl.copy(format = null).toPollLine()).isEqualTo("Avengers: Endgame Extra (Giovedì - 20:15 - UCI Luxe Marcon)")
    }

    @Test
    fun sameTimeInDifferentFormatsIsNotMerged() {
        val day = LocalDate(2026, 10, 1)
        val xl = Showing("Avengers", day, LocalTime.parse("20:15"), venueUci, "XL")
        val twoD = xl.copy(format = "2D")
        assertThat(listOf(xl, twoD).distinct().size).isEqualTo(2)
    }

    @Test
    fun cleanTitleDropsRatingSuffix() {
        assertThat(cleanTitle("Mammina - V.M.14")).isEqualTo("Mammina")
        assertThat(cleanTitle("MAMMINA - V.M.14")).isEqualTo("Mammina")
        assertThat(cleanTitle("Mammina (V.M.14)")).isEqualTo("Mammina")
        assertThat(filmKey(cleanTitle("Mammina - V.M.14"))).isEqualTo(filmKey("Mammina"))
    }

    @Test
    fun cleanTitleConvertsAllCapsToTitleCaseAndKeepsOthers() {
        assertThat(cleanTitle("AVENGERS: ENDGAME EXTRA")).isEqualTo("Avengers: Endgame Extra")
        assertThat(cleanTitle("Avengers:  Endgame Extra ")).isEqualTo("Avengers: Endgame Extra")
        assertThat(cleanTitle("Linkin Park: Unshatter C.A.")).isEqualTo("Linkin Park: Unshatter C.A.")
    }

    @Test
    fun filmKeyHandlesPunctuationAndAccentVariantsCorrectly() {
        // Periods: "Vs." should match "Vs"
        assertThat(filmKey("Coyote Vs. Acme")).isEqualTo(filmKey("Coyote Vs Acme"))

        // Apostrophes and backticks: different apostrophe variants should match
        assertThat(filmKey("L`Isola Dei Ricordi")).isEqualTo(filmKey("L'Isola Dei Ricordi"))

        // Multiple punctuation: various punctuation should be ignored
        assertThat(filmKey("Toy Story.")).isEqualTo(filmKey("Toy Story"))

        // Commas and other punctuation
        assertThat(filmKey("Hello World")).isEqualTo(filmKey("Hello, World!"))

        // Mixed: accents with punctuation
        assertThat(filmKey("L'Ete")).isEqualTo(filmKey("L'Été"))
        assertThat(filmKey("LÉte")).isEqualTo(filmKey("L'Été"))
    }

    @Test
    fun filmKeyTreatsSentenceSubtitleAsDashSubtitle() {
        assertThat(filmKey("Carlo Acutis. Il Giovane Santo")).isEqualTo(filmKey("Carlo Acutis - Il giovane santo"))
        assertThat(filmKey("Carlo Acutis. Il Giovane Santo")).isEqualTo("carlo acutis il giovane santo")

        // Abbreviations are not subtitle separators
        assertThat(filmKey("Dr. Strange")).isEqualTo("dr strange")
    }

    @Test
    fun sameFilmWithPunctuationVariantsIsGrouped() {
        val groups = listOf(
            showing("Coyote Vs Acme", thursday, "19:00", venueTheSpace),
            showing("Coyote Vs. Acme", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(1)
        assertThat(groups.first().title).isEqualTo("Coyote Vs Acme")
        assertThat(groups.first().showings.size).isEqualTo(2)
    }

    @Test
    fun searchTitleDropsOnlyFormatLanguageAndRatingSuffixes() {
        assertThat(searchTitle("Dune (3D)")).isEqualTo("Dune")
        assertThat(searchTitle("Avatar (IMAX)")).isEqualTo("Avatar")
        assertThat(searchTitle("Café - Lingua originale")).isEqualTo("Café")
        assertThat(searchTitle("LINKIN PARK UNSHATTER - LINGUA ORIGINALE")).isEqualTo("Linkin Park Unshatter")
        assertThat(searchTitle("Naza C.A.")).isEqualTo("Naza")
        assertThat(searchTitle("Cars - 20 Anni")).isEqualTo("Cars")
        assertThat(searchTitle("Mammina - V.M.14")).isEqualTo("Mammina")
        assertThat(searchTitle("Mammina (V.M.14)")).isEqualTo("Mammina")
        assertThat(searchTitle("Mammina V.M. 14")).isEqualTo("Mammina")
    }

    @Test
    fun searchTitleKeepsAccentsPunctuationAndRealSubtitles() {
        assertThat(searchTitle("Coyote Vs. Acme")).isEqualTo("Coyote Vs. Acme")
        assertThat(searchTitle("L'Été")).isEqualTo("L'Été")
        assertThat(searchTitle("Avengers: Endgame")).isEqualTo("Avengers: Endgame")
        assertThat(searchTitle("Cars - Motori Ruggenti")).isEqualTo("Cars - Motori Ruggenti")
        assertThat(searchTitle("Film (Parte 2)")).isEqualTo("Film (Parte 2)")
    }

    @Test
    fun titleKeyIgnoresCaseAccentsAndPunctuationButCutsNothing() {
        assertThat(titleKey("Café")).isEqualTo("cafe")
        assertThat(titleKey("Coyote Vs. Acme")).isEqualTo("coyote vs acme")
        assertThat(titleKey("L'Été")).isEqualTo("lete")
        assertThat(titleKey("Dune: Part Two")).isEqualTo("dune part two")
        assertThat(titleKey("Star Wars: Episode III - Revenge of the Sith")).isEqualTo("star wars episode iii revenge of the sith")
        assertThat(titleKey("千と千尋の神隠し")).isEqualTo("千と千尋の神隠し")
        assertThat(titleKey(" !!! ")).isEqualTo("")
        // `filmKey` cuts the subtitle: this is why matching must not use it.
        assertThat(filmKey("Star Wars: Episode III - Revenge of the Sith")).isEqualTo("star wars episode iii revenge of the sith")
    }

    @Test
    fun searchTitleLeavesFilmKeyResultsUnchanged() {
        assertThat(filmKey("Avatar (IMAX)")).isEqualTo("avatar")
        assertThat(filmKey("LINKIN PARK UNSHATTER - LINGUA ORIGINALE")).isEqualTo("linkin park unshatter")
        assertThat(filmKey("Cars: Motori Ruggenti - 20 Anni")).isEqualTo(filmKey("Cars - Motori Ruggenti"))
    }

    @Test
    fun filmKeyHandlesColonAndDashSubtitleSeparators() {
        // Colon and dash separators should produce the same key
        assertThat(filmKey("Cars: Motori Ruggenti - 20 Anni")).isEqualTo(filmKey("Cars - Motori Ruggenti"))
    }

    @Test
    fun carsMovieVariantsAreGroupedTogether() {
        val groups = listOf(
            showing("Cars - Motori Ruggenti", thursday, "17:00", venueTheSpace),
            showing("Cars: Motori Ruggenti - 20 Anni", thursday, "19:00", venueUci)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(1)
        assertThat(groups.first().title).isEqualTo("Cars - Motori Ruggenti")
        assertThat(groups.first().showings.size).isEqualTo(2)
    }

    @Test
    fun filmKeyHandlesColonWithLanguageSuffix() {
        // Colon with language suffix should keep both title and subtitle
        assertThat(filmKey("LINKIN PARK UNSHATTER - LINGUA ORIGINALE")).isEqualTo(filmKey("Linkin Park: Unshatter"))
        // Both should normalize to "linkin park unshatter"
        assertThat(filmKey("Linkin Park: Unshatter")).isEqualTo("linkin park unshatter")
        assertThat(filmKey("LINKIN PARK UNSHATTER - LINGUA ORIGINALE")).isEqualTo("linkin park unshatter")
    }

    @Test
    fun colonAndDashSubtitleVariantsAreGroupedTogether() {
        val groups = listOf(
            showing("Carlo Acutis: Il Giovane Santo", thursday, "19:00", venueTheSpace),
            showing("Carlo Acutis - Il Giovane Santo", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(1)
        assertThat(groups.first().showings.size).isEqualTo(2)
    }

    @Test
    fun titlesWithTheSameDatabaseIdAreOneFilmWhateverTheirTitles() {
        val groups = listOf(
            showing("HEART OF THE BEAST - NEL PROFONDO SELVAGGIO", thursday, "21:40", venueTheSpace),
            showing("Heart of the Beast", thursday, "19:40", venueUci)
        ).groupByFilm(mapOf(
            "HEART OF THE BEAST - NEL PROFONDO SELVAGGIO" to 42,
            "Heart of the Beast" to 42
        ))

        assertThat(groups.size).isEqualTo(1)
        assertThat(groups.first().showings.size).isEqualTo(2)
    }

    @Test
    fun differentDatabaseIdsStaySeparateEvenWhenTitlesMatch() {
        val groups = listOf(
            showing("Avengers: Endgame", thursday, "19:00", venueTheSpace),
            showing("Avengers: Endgame Extra", thursday, "21:00", venueUci)
        ).groupByFilm(mapOf(
            "Avengers: Endgame" to 1,
            "Avengers: Endgame Extra" to 2
        ))

        assertThat(groups.size).isEqualTo(2)
    }

    @Test
    fun showingsWithoutAnIdAreGroupedByTitleAsBefore() {
        val showings = listOf(
            showing("Naza", friday, "21:40", venueTheSpace),
            showing("Naza C.A.", friday, "19:40", venueUci)
        )

        assertThat(showings.groupByFilm().size).isEqualTo(1)
        assertThat(showings.groupByFilm(emptyMap()).size).isEqualTo(1)
    }

    @Test
    fun aShowingWithAnIdIsNotMergedWithOneWithoutUntilItsIdIsKnown() {
        val groups = listOf(
            showing("Naza", friday, "21:40", venueTheSpace),
            showing("Naza C.A.", friday, "19:40", venueUci)
        ).groupByFilm(mapOf("Naza" to 7))

        assertThat(groups.size).isEqualTo(2)
    }

    @Test
    fun avengersWithAndWithoutExtraAreNotGroupedTogether() {
        val groups = listOf(
            showing("Avengers: Endgame", thursday, "19:00", venueTheSpace),
            showing("Avengers: Endgame Extra", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(2)
    }

    @Test
    fun linkinParkVariantsAreGroupedTogether() {
        val groups = listOf(
            showing("Linkin Park: Unshatter", thursday, "19:00", venueTheSpace),
            showing("LINKIN PARK UNSHATTER - LINGUA ORIGINALE", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(1)
        assertThat(groups.first().title).isEqualTo("Linkin Park: Unshatter")
        assertThat(groups.first().showings.size).isEqualTo(2)
    }

    @Test
    fun filmKeyStripsTrailingFormatSuffixInParentheses() {
        // Format suffixes in trailing parentheses should be stripped
        assertThat(filmKey("Avengers: Endgame Extra (Infinity Vision)")).isEqualTo(filmKey("Avengers: Endgame Extra"))
        assertThat(filmKey("Avatar (IMAX)")).isEqualTo(filmKey("Avatar"))
        assertThat(filmKey("Avengers: Endgame Extra (Infinity Vision)")).isEqualTo("avengers endgame extra")
    }

    @Test
    fun avengersAndAvengersWithInfinityVisionAreGroupedTogether() {
        val groups = listOf(
            showing("Avengers: Endgame Extra", thursday, "19:00", venueTheSpace),
            showing("Avengers: Endgame Extra (Infinity Vision)", thursday, "21:00", venueUci)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(1)
        assertThat(groups.first().title).isEqualTo("Avengers: Endgame Extra")
        assertThat(groups.first().showings.size).isEqualTo(2)
    }

    @Test
    fun unrelatedParenthesesAreNotStripped() {
        // Parentheses without format keywords should not be stripped
        assertThat(filmKey("Film (Parte 2)")).isEqualTo(filmKey("Film (Parte 2)"))
        assertThat(filmKey("Avatar 2D") == filmKey("Avatar 3D")).isFalse()
        assertThat(filmKey("Avatar XL") == filmKey("Avatar")).isFalse()
    }

    @Test
    fun avatarVariantsRemainSeparate() {
        // Avatar in different formats should NOT be grouped together
        val groups = listOf(
            showing("Avatar 2D", thursday, "19:00", venueTheSpace),
            showing("Avatar 3D", thursday, "19:00", venueUci),
            showing("Avatar XL", thursday, "19:00", venueNotorious)
        ).groupByFilm()

        assertThat(groups.size).isEqualTo(3)
    }
}
