package com.preichert.cinemerick.feature.showtimes.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilmDetailsTextTest {

    @Test
    fun paragraphsBecomeLinesAndEntitiesAreDecoded() {
        assertEquals("Prima &\nSeconda", "<p>Prima &amp;</p><p></p><p> Seconda </p>".htmlToPlainText())
    }

    @Test
    fun lineBreaksSplitLines() {
        assertEquals("a\nb", "a<br>b".htmlToPlainText())
    }

    @Test
    fun emptyOrTagOnlyInputGivesNull() {
        assertNull(null.htmlToPlainText())
        assertNull("<p></p>".htmlToPlainText())
    }

    @Test
    fun joinedOrNullSkipsBlanks() {
        assertEquals("a, b", listOf("a", " ", "b").joinedOrNull())
        assertNull(listOf(" ").joinedOrNull())
    }
}
