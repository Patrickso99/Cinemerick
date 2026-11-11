package com.preichert.cinemerick.feature.showtimes.data

import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

class FilmDetailsTextTest {

    @Test
    fun paragraphsBecomeLinesAndEntitiesAreDecoded() {
        assertThat("<p>Prima &amp;</p><p></p><p> Seconda </p>".htmlToPlainText()).isEqualTo("Prima &\nSeconda")
    }

    @Test
    fun lineBreaksSplitLines() {
        assertThat("a<br>b".htmlToPlainText()).isEqualTo("a\nb")
    }

    @Test
    fun emptyOrTagOnlyInputGivesNull() {
        assertThat(null.htmlToPlainText()).isNull()
        assertThat("<p></p>".htmlToPlainText()).isNull()
    }

    @Test
    fun joinedOrNullSkipsBlanks() {
        assertThat(listOf("a", " ", "b").joinedOrNull()).isEqualTo("a, b")
        assertThat(listOf(" ").joinedOrNull()).isNull()
    }
}
