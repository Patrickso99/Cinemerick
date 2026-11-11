package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.size
import org.jetbrains.compose.resources.painterResource
import com.preichert.cinemerick.core.designsystem.resources.Res
import com.preichert.cinemerick.core.designsystem.resources.cd_clear
import com.preichert.cinemerick.core.designsystem.resources.ic_close
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.minimumInteractiveComponentSize

/**
 * Compact time box on the numeric keypad: small [label] above a large value. Digits are kept and a colon is
 * inserted after the hour, so `HH` and `HH:MM` can both be entered without a text keyboard.
 * Pass [errorText] to flag invalid input; a clear button appears when not blank, [icon] decorates the end.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    icon: Painter? = null,
) {
    val focusManager = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    // Sync local state when external value changes (e.g., from ViewModel or clear button)
    if (field.text != value) {
        field = TextFieldValue(value, TextRange(value.length))
    }

    var focused by remember { mutableStateOf(false) }
    val bringIntoView = remember { BringIntoViewRequester() }
    val imeBottom = WindowInsets.ime
    val density = androidx.compose.ui.platform.LocalDensity.current
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val extra = with(density) { 96.dp.toPx() }
    // Once the keyboard has opened (insets applied), scroll the focused box above it.
    LaunchedEffect(focused) {
        if (focused) {
            // Re-run on every inset change: the scroll range only grows as the keyboard animates in.
            snapshotFlow { imeBottom.getBottom(density) }.collect { if (it > 0) bringIntoView.bringIntoView(Rect(0f, 0f, size.width.toFloat(), size.height.toFloat() + extra)) }
        }
    }
    val borderColor = if (errorText != null) scheme.error else scheme.outlineVariant
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size = it }
                .bringIntoViewRequester(bringIntoView)
                .clip(MaterialTheme.shapes.medium)
                .border(1.dp, borderColor, MaterialTheme.shapes.medium)
                .padding(start = 13.dp, end = 13.dp, top = 12.dp, bottom = 13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicTextField(
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
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface,
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                        onDone = { focusManager.clearFocus() }
                    ),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty() && focused) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
                )
                if (value.isNotEmpty()) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = stringResource(Res.string.cd_clear),
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .minimumInteractiveComponentSize()
                            .clickable(role = Role.Button) { onValueChange("") },
                    )
                }
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = scheme.onSurfaceVariant)
                }
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

internal fun String.toNumericTime(): String {
    val digits = filter(Char::isDigit).take(4)
    return if (digits.length <= 2) digits else "${digits.take(2)}:${digits.drop(2)}"
}
