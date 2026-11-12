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
}
