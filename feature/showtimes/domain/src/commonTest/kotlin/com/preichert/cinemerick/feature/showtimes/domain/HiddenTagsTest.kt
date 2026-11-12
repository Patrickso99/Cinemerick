package com.preichert.cinemerick.feature.showtimes.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import kotlin.test.Test

class HiddenTagsTest {

    @Test
    fun onlyTwoDHidesThreeDAndPremiumScreensButKeepsOriginalLanguage() {
        val hidden = hiddenTagsFor(only = setOf("2D"), excluded = emptySet(), knownTags = emptySet())

        assertThat(hidden).containsExactlyInAnyOrder("3D", "IMAX", "4DX", "SCREENX", "DOLBY ATMOS", "INFINITY VISION", "EPIC")
    }

    @Test
    fun onlyAlsoHidesTagsSeenInTheResultsThatAreNotInTheCommonList() {
        val hidden = hiddenTagsFor(only = setOf("2D"), excluded = emptySet(), knownTags = setOf("Sala Energia"))

        assertThat(hidden).contains("SALA ENERGIA")
    }

    @Test
    fun onlyOriginalLanguageKeepsBothDimensions() {
        val hidden = hiddenTagsFor(only = setOf("VO"), excluded = emptySet(), knownTags = emptySet())

        assertThat(hidden).doesNotContain("2D")
        assertThat(hidden).doesNotContain("3D")
        assertThat(hidden).doesNotContain("VO")
    }

    @Test
    fun excludedOnlyHidesThoseTags() {
        val hidden = hiddenTagsFor(only = emptySet(), excluded = setOf("3d"), knownTags = setOf("2D", "3D", "IMAX"))

        assertThat(hidden).containsExactlyInAnyOrder("3D")
    }

    @Test
    fun nothingSaidHidesNothing() {
        assertThat(hiddenTagsFor(emptySet(), emptySet(), setOf("3D", "IMAX"))).isEmpty()
    }

    @Test
    fun aFormatBothOnlyAndExcludedEndsUpHidden() {
        // Contradictory request: excluding wins, so nothing the user refused is shown.
        val hidden = hiddenTagsFor(only = setOf("IMAX"), excluded = setOf("IMAX"), knownTags = emptySet())

        assertThat(hidden).contains("IMAX")
    }
}
