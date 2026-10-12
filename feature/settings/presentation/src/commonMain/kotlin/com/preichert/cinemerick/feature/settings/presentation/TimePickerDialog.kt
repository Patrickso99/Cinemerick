package com.preichert.cinemerick.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.presentation.minutesOf
import com.preichert.cinemerick.core.presentation.toHour
import com.preichert.cinemerick.core.presentation.toMinute
import com.preichert.cinemerick.feature.settings.presentation.resources.Res
import com.preichert.cinemerick.feature.settings.presentation.resources.time_window_cancel
import com.preichert.cinemerick.feature.settings.presentation.resources.time_window_no_limit
import com.preichert.cinemerick.feature.settings.presentation.resources.time_window_ok
import org.jetbrains.compose.resources.stringResource

/**
 * 24h time picker dialog. The picked time is a draft: [onConfirm] is the only way a value
 * leaves the dialog, and it is disabled while [isValid] rejects the draft.
 * [onNoLimit] adds a "No limit" button (used for the maximum).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(
    title: String,
    initialMinutes: Int,
    isValid: (Int) -> Boolean,
    errorText: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    onNoLimit: (() -> Unit)? = null,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialMinutes.toHour(),
        initialMinute = initialMinutes.toMinute(),
        is24Hour = true,
    )
    val draft = minutesOf(pickerState.hour, pickerState.minute)
    val valid = isValid(draft)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                TimePicker(state = pickerState)
                if (!valid) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }, enabled = valid) {
                Text(stringResource(Res.string.time_window_ok))
            }
        },
        dismissButton = {
            Column {
                if (onNoLimit != null) {
                    TextButton(onClick = onNoLimit) { Text(stringResource(Res.string.time_window_no_limit)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.time_window_cancel)) }
            }
        },
    )
}

@DevicePreviews
@Composable
private fun SettingsScreenPreview() {
    CinemerickTheme {
        TimePickerDialog(
            title = "Pick a time",
            initialMinutes = 750, // 12:30
            isValid = { true },
            errorText = "Invalid time",
            onConfirm = {},
            onDismiss = {},
            onNoLimit = {}
        )
    }
}
