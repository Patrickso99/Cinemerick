package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Compact time field on the numeric keypad. Digits are kept and a colon is inserted after the hour,
 * so `HH` and `HH:MM` can both be entered without a text keyboard.
 * Pass [errorText] to flag invalid input; a clear button appears when not blank.
 */
@Composable
fun TimeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    // Sync local state when external value changes (e.g., from ViewModel or clear button)
    if (field.text != value) {
        field = TextFieldValue(value, TextRange(value.length))
    }

    OutlinedTextField(
        value = field,
        onValueChange = { newField ->
            val formatted = newField.text.toNumericTime()
            val newSelection = if (formatted != newField.text) {
                // Text was reformatted (colon added/removed); move cursor to end
                TextRange(formatted.length)
            } else {
                // Text unchanged; preserve user's cursor position
                newField.selection
            }
            field = TextFieldValue(formatted, newSelection)
            onValueChange(formatted)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        trailingIcon = if (value.isNotEmpty()) {
            {
                TextButton(onClick = { onValueChange("") }) {
                    Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else null,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            onDone = { focusManager.clearFocus() }
        ),
        modifier = modifier
    )
}

internal fun String.toNumericTime(): String {
    val digits = filter(Char::isDigit).take(4)
    return if (digits.length <= 2) digits else "${digits.take(2)}:${digits.drop(2)}"
}
