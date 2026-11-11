package com.preichert.cinemerick.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Editing buffer (text and cursor) of a text field whose text is owned by a ViewModel.
 *
 * With `BasicTextField(value: String, …)` the text makes a round trip through the screen state before the field sees
 * it. When that state arrives a frame late (it does on iOS, e.g. in a bottom sheet), the field is first recomposed
 * with the old text, the cursor is clamped to it, and the next letter lands after the cursor: the cursor ends up on
 * the left of what was typed. Here the field always shows the buffer; the owner's value replaces it only when it is
 * a real change from outside (Clear, a restored filter), never when it is the echo of the user's own typing.
 *
 * A change is noticed when the owner's value differs from the last one seen. If the owner went back to that same value
 * before it ever saw the typing (in the same frame), the two cannot be told apart and the buffer is kept.
 */
@Stable
class SyncedTextFieldState(initial: String) {
    var value: TextFieldValue by mutableStateOf(TextFieldValue(initial, TextRange(initial.length)))
        private set

    // Texts sent to the owner that have not come back yet, oldest first.
    private val pending = ArrayDeque<String>()
    private var lastExternal = initial

    /** The user edited the field. Returns true when the text changed, i.e. when the owner must be told. */
    fun onEdit(edited: TextFieldValue): Boolean {
        val changed = edited.text != value.text
        value = edited
        if (changed) pending.addLast(edited.text)
        return changed
    }

    /** The owner's current text. Only a change that is not an echo of [onEdit] replaces the buffer. */
    fun sync(external: String) {
        if (external == lastExternal) return
        lastExternal = external
        val echo = pending.indexOf(external)
        when {
            echo >= 0 -> repeat(echo + 1) { pending.removeFirst() }
            external == value.text -> pending.clear()
            else -> {
                value = TextFieldValue(external, TextRange(external.length))
                pending.clear()
            }
        }
    }
}

/** A [SyncedTextFieldState] kept in step with [value], the text the owner holds. */
@Composable
fun rememberSyncedTextFieldState(value: String): SyncedTextFieldState {
    val state = remember { SyncedTextFieldState(value) }
    state.sync(value)
    return state
}
