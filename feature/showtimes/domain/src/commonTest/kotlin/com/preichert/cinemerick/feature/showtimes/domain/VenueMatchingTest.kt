package com.preichert.cinemerick.feature.showtimes.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlin.test.Test

class VenueMatchingTest {

    private val silea = Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
    private val pradamano = Venue(Chain.THE_SPACE, "1010", "Pradamano", "Friuli")
    private val marcon = Venue(Chain.UCI, "uci-marcon", "UCI Luxe Marcon", "Veneto")
    private val villesse = Venue(Chain.UCI, "uci-villesse", "UCI Villesse", "Friuli")
    private val uciFiume = Venue(Chain.UCI, "uci-fiume", "UCI Luxe Fiume Veneto", "Friuli")
    private val notoriousFiume = Venue(Chain.NOTORIOUS, "fiumeveneto", "Fiume Veneto", "Friuli")
    private val cristallo = Venue(Chain.CRISTALLO, "oderzo", "Cristallo Oderzo", "Veneto")

    private val catalog = mapOf(
        Chain.THE_SPACE to listOf(silea, pradamano),
        Chain.UCI to listOf(marcon, villesse, uciFiume),
        Chain.NOTORIOUS to listOf(notoriousFiume),
        Chain.CRISTALLO to listOf(cristallo),
    )

    @Test
    fun chainAndPlaceSaidAsSpelledAreCertain() {
        val match = matchVenue(VenueMention("UCI Marcon", Chain.UCI, "Marcon"), catalog)

        assertThat(match).isEqualTo(VenueMatch.Certain(marcon))
    }

    @Test
    fun chainMisspelledInSpeechIsOnlyCloseEvenWhenTheAssistantCorrectedIt() {
        val match = matchVenue(VenueMention("Notorius Fiume", Chain.NOTORIOUS, "Fiume"), catalog)

        assertThat(match).isEqualTo(VenueMatch.Close(notoriousFiume, listOf(notoriousFiume)))
    }

    @Test
    fun placeMisspelledIsClose() {
        val match = matchVenue(VenueMention("UCI Villese", Chain.UCI, "Villese"), catalog)

        assertThat(match).isInstanceOf<VenueMatch.Close>().prop(VenueMatch.Close::best).isEqualTo(villesse)
    }

    @Test
    fun cinemaWithNoNearNameIsNotFound() {
        val match = matchVenue(VenueMention("Cinema Astra", null, "Astra"), catalog)

        assertThat(match).isEqualTo(VenueMatch.NotFound)
    }

    @Test
    fun chainInferredFromTheSpokenWordsWhenTheAssistantGaveNone() {
        val match = matchVenue(VenueMention("the space silea"), catalog)

        assertThat(match).isEqualTo(VenueMatch.Certain(silea))
    }

    @Test
    fun accentsAndCapitalsDoNotMatter() {
        val match = matchVenue(VenueMention("THE SPACE SÌLEA", Chain.THE_SPACE, "SÌLEA"), catalog)

        assertThat(match).isEqualTo(VenueMatch.Certain(silea))
    }

    @Test
    fun onlyTheChainNamedPrefersAVenueAlreadySelected() {
        val match = matchVenue(VenueMention("UCI", Chain.UCI, null), catalog, preferred = setOf(villesse))

        assertThat(match).isInstanceOf<VenueMatch.Close>().prop(VenueMatch.Close::best).isEqualTo(villesse)
    }

    @Test
    fun onlyTheChainNamedWithASingleVenueIsCertain() {
        val match = matchVenue(VenueMention("Cristallo", Chain.CRISTALLO, null), catalog)

        assertThat(match).isEqualTo(VenueMatch.Certain(cristallo))
    }

    @Test
    fun placeShortForSeveralVenuesIsNotCertain() {
        // "Fiume" is in both the UCI and the Notorious venue: without a chain it must be asked.
        val match = matchVenue(VenueMention("Fiume"), catalog)

        assertThat(match).isInstanceOf<VenueMatch.Close>()
    }

    @Test
    fun chainWhoseVenuesDidNotLoadIsNotFound() {
        val match = matchVenue(VenueMention("Notorious Fiume", Chain.NOTORIOUS, "Fiume"), catalog - Chain.NOTORIOUS)

        assertThat(match).isEqualTo(VenueMatch.NotFound)
    }

    @Test
    fun shortWordsAreNeverMatchedBySpelling() {
        // "Ucy" is one letter from "uci", but three-letter words only match exactly.
        val match = matchVenue(VenueMention("Ucy Marcon"), catalog)

        assertThat(match).isInstanceOf<VenueMatch.Close>().prop(VenueMatch.Close::best).isEqualTo(marcon)
    }

    @Test
    fun blankOrEmojiOnlyMentionIsNotFound() {
        listOf("", "   ", "🎬🍿", "👨‍👩‍👧‍👦").forEach { spoken ->
            assertThat(matchVenue(VenueMention(spoken), catalog), name = "spoken=\"$spoken\"").isEqualTo(VenueMatch.NotFound)
        }
    }

    // ---------- The words of the user against the reading of the model ----------

    @Test
    fun theWordsOfTheUserWinWhenTheModelAttachedAnotherChainToThem() {
        // A model answered "UCI marcon" with chain THE_SPACE and place "silea".
        val match = matchVenue(VenueMention("UCI marcon", Chain.THE_SPACE, "silea"), catalog)

        assertThat(match).isEqualTo(VenueMatch.Certain(marcon))
    }

    @Test
    fun aMisspeltNameStillGetsItsOwnChainWhenTheModelWasWrong() {
        val match = matchVenue(VenueMention("Uci Villese", Chain.THE_SPACE, "silea"), catalog)

        assertThat(match).isInstanceOf<VenueMatch.Close>().prop(VenueMatch.Close::best).isEqualTo(villesse)
    }

    @Test
    fun theModelsChainIsUsedWhenTheWordsNameNoneButItIsOnlyAskedAbout() {
        // "Silea" alone: the model knows it is The Space's, but the user did not say so.
        val match = matchVenue(VenueMention("Silea", Chain.THE_SPACE, "Silea"), catalog)

        assertThat(match).isEqualTo(VenueMatch.Close(silea, listOf(silea)))
    }

    // ---------- Mentions that hold several cinemas ----------

    @Test
    fun aMentionNamingSeveralChainsIsSplitWhereEachChainStarts() {
        val pieces = splitVenueMention(VenueMention("the space silea uci marcon notorius fiume", Chain.THE_SPACE, "Fiume Veneto"))

        assertThat(pieces.map { it.spoken }).isEqualTo(listOf("the space silea", "uci marcon", "notorius fiume"))
        assertThat(pieces.map { it.chain }).isEqualTo(listOf(null, null, null))
    }

    @Test
    fun theSplitPiecesMatchTheirVenues() {
        val matches = splitVenueMention(VenueMention("the space silea uci marcon notorious fiume veneto"))
            .map { matchVenue(it, catalog) }

        assertThat(matches).isEqualTo(
            listOf(VenueMatch.Certain(silea), VenueMatch.Certain(marcon), VenueMatch.Certain(notoriousFiume))
        )
    }

    @Test
    fun twoVenuesOfTheSameChainInOneMentionAreSplitToo() {
        val pieces = splitVenueMention(VenueMention("uci marcon uci villesse"))

        assertThat(pieces.map { it.spoken }).isEqualTo(listOf("uci marcon", "uci villesse"))
    }

    @Test
    fun aMentionWithOneChainOrNoneIsLeftAsItIs() {
        listOf("The Space Silea", "UCI Cinemas Marcon", "Cinema Astra", "Notorius Fiume", "", "  ", "🎬").forEach { spoken ->
            val mention = VenueMention(spoken, Chain.UCI, "x")
            assertThat(splitVenueMention(mention), name = "spoken=\"$spoken\"").isEqualTo(listOf(mention))
        }
    }

    @Test
    fun aFillerWordIsNeverTakenForAChain() {
        // "cinema" is part of "Cinema Zero" but also of any cinema's name.
        val mention = VenueMention("cinema astra cinema roma")

        assertThat(splitVenueMention(mention)).isEqualTo(listOf(mention))
    }
}
