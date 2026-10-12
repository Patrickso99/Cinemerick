package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_cancel
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_ok
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_title
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * Multi-day picker dialog. The selection is a draft: [onConfirm] is the only way a value
 * leaves the dialog, and it is disabled while the draft is empty.
 */
@Composable
internal fun DatePickerDialog(
    initialDates: Set<LocalDate>,
    minDate: LocalDate,
    onConfirm: (Set<LocalDate>) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(initialDates) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.calendar_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MultiDateCalendar(
                    selected = draft,
                    minDate = minDate,
                    onToggle = { date -> draft = if (date in draft) draft - date else draft + date }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }, enabled = draft.isNotEmpty()) {
                Text(stringResource(Res.string.calendar_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.calendar_cancel)) }
        },
    )
}

// Previews - Light theme

@DevicePreviews
@Composable
private fun DatePickerDialogPreviewLight() {
    LightThemePreview {
        DatePickerDialog(
            initialDates = setOf(LocalDate(2026, 10, 1), LocalDate(2026, 10, 2)),
            minDate = LocalDate(2026, 10, 1),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

// Previews - Dark theme

@DevicePreviews
@Composable
private fun DatePickerDialogPreviewDark() {
    DarkThemePreview {
        DatePickerDialog(
            initialDates = setOf(LocalDate(2026, 10, 1), LocalDate(2026, 10, 2)),
            minDate = LocalDate(2026, 10, 1),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

// Empty draft: OK is disabled

@DevicePreviews
@Composable
private fun DatePickerDialogPreviewEmpty() {
    LightThemePreview {
        DatePickerDialog(
            initialDates = emptySet(),
            minDate = LocalDate(2026, 10, 1),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
