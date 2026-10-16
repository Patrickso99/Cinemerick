package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.preichert.cinemerick.core.designsystem.CinemerickBottomSheet
import com.preichert.cinemerick.core.designsystem.CinemerickFilterChip
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickSecondaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_done_count
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_subtitle
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.clear
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * Multi-day calendar in a bottom sheet, as in docs/design (Phone, 06): the month grid, the chosen days as chips
 * (tap a chip to remove it), and Clear and Done. Dismissing the sheet keeps the days as they were before it opened.
 */
@Composable
internal fun DatePickerDialog(
    initialDates: Set<LocalDate>,
    minDate: LocalDate,
    onConfirm: (Set<LocalDate>) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(initialDates) }
    val sortedDraft = draft.sorted()

    CinemerickBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.space5, end = Spacing.space5, bottom = Spacing.space5),
            verticalArrangement = Arrangement.spacedBy(Spacing.space4),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
                Text(
                    text = stringResource(Res.string.calendar_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = CinemerickTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(Res.string.calendar_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemerickTheme.colors.onSurfaceVariant,
                )
            }

            MultiDateCalendar(
                selected = draft,
                minDate = minDate,
                onToggle = { date -> draft = if (date in draft) draft - date else draft + date },
            )

            if (sortedDraft.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
                    verticalArrangement = Arrangement.spacedBy(Spacing.space2),
                ) {
                    sortedDraft.forEach { date ->
                        CinemerickFilterChip(
                            label = "${date.chipDayName()} ${date.day}",
                            selected = true,
                            leadingIcon = CinemerickIcons.Close,
                            onClick = { draft = draft - date },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                CinemerickSecondaryButton(
                    text = stringResource(Res.string.clear),
                    onClick = { draft = emptySet() },
                    enabled = draft.isNotEmpty(),
                )
                CinemerickPrimaryButton(
                    text = stringResource(Res.string.calendar_done_count, draft.size),
                    onClick = { onConfirm(draft) },
                    enabled = draft.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// Previews - Light theme

@Preview
@Composable
private fun DatePickerDialogPreviewLight() {
    LightThemePreview {
        DatePickerDialog(
            initialDates = setOf(LocalDate(2026, 10, 17), LocalDate(2026, 10, 18)),
            minDate = LocalDate(2026, 10, 16),
            onConfirm = {},
            onDismiss = {},
        )
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun DatePickerDialogPreviewDark() {
    DarkThemePreview {
        DatePickerDialog(
            initialDates = setOf(LocalDate(2026, 10, 17), LocalDate(2026, 10, 18)),
            minDate = LocalDate(2026, 10, 16),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
