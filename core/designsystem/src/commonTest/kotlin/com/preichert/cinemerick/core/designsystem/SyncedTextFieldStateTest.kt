package com.preichert.cinemerick.core.designsystem

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class SyncedTextFieldStateTest {

    private fun typed(text: String) = TextFieldValue(text, TextRange(text.length))

    @Test
    fun aLateEchoOfTheTypedTextKeepsTheCursorAfterIt() {
        val state = SyncedTextFieldState("")

        state.onEdit(typed("u"))
        // The owner has not answered yet: the field recomposes with the old text first, then the echo arrives.
        state.sync("")
        state.sync("u")

        assertThat(state.value).isEqualTo(typed("u"))
    }

    @Test
    fun fastTypingSurvivesEchoesThatArriveOneByOne() {
        val state = SyncedTextFieldState("")

        state.onEdit(typed("U"))
        state.onEdit(typed("UC"))
        state.onEdit(typed("UCI"))
        state.sync("U")
        state.sync("UC")

        assertThat(state.value).isEqualTo(typed("UCI"))
        state.sync("UCI")
        assertThat(state.value).isEqualTo(typed("UCI"))
    }

    @Test
    fun anEchoThatSkipsIntermediateTextsIsStillRecognised() {
        val state = SyncedTextFieldState("")

        state.onEdit(typed("S"))
        state.onEdit(typed("Si"))
        state.onEdit(typed("Sil"))
        state.sync("Si")

        assertThat(state.value).isEqualTo(typed("Sil"))
    }

    @Test
    fun aClearFromTheOwnerWhileTypingEmptiesTheField() {
        val state = SyncedTextFieldState("")
        state.onEdit(typed("Dun"))
        state.sync("Dun")
        state.onEdit(typed("Dune"))

        // Clear is a separate tap, so it arrives after the owner has seen the typing.
        state.sync("")

        assertThat(state.value).isEqualTo(typed(""))
    }

    @Test
    fun aRestoredTextFromTheOwnerPutsTheCursorAtItsEnd() {
        val state = SyncedTextFieldState("")

        state.sync("Dune, Wicked")

        assertThat(state.value).isEqualTo(typed("Dune, Wicked"))
    }

    @Test
    fun anOwnerThatRewritesTheTextWins() {
        val state = SyncedTextFieldState("")
        state.onEdit(typed("Dune "))

        // e.g. the owner trims what it stores: not an echo, so the field shows the owner's text.
        state.sync("Dune")

        assertThat(state.value).isEqualTo(typed("Dune"))
    }

    @Test
    fun movingTheCursorIsNotAnEditForTheOwner() {
        val state = SyncedTextFieldState("Wicked")

        val changed = state.onEdit(TextFieldValue("Wicked", TextRange(2)))

        assertThat(changed).isFalse()
        assertThat(state.value.selection).isEqualTo(TextRange(2))
    }

    @Test
    fun anEditInTheMiddleKeepsTheCursorThereThroughTheEcho() {
        val state = SyncedTextFieldState("Wcked")

        assertThat(state.onEdit(TextFieldValue("Wicked", TextRange(2)))).isTrue()
        state.sync("Wicked")

        assertThat(state.value).isEqualTo(TextFieldValue("Wicked", TextRange(2)))
    }

    @Test
    fun emojiAndAccentsKeepTheCursorAtTheEnd() {
        val state = SyncedTextFieldState("")

        state.onEdit(typed("Amélie 🎬"))
        state.sync("Amélie 🎬")
        state.sync("Amélie 🎬")

        assertThat(state.value).isEqualTo(typed("Amélie 🎬"))
    }

    @Test
    fun typingBackTheSameTextAsTheOwnerHoldsIsNotReplaced() {
        val state = SyncedTextFieldState("UCI")

        state.onEdit(typed("UC"))
        state.onEdit(typed("UCI"))
        state.sync("UC")

        assertThat(state.value).isEqualTo(typed("UCI"))
    }
}
