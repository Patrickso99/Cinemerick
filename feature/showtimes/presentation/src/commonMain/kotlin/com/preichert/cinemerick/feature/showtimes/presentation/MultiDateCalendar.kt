package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.domain.italianName
import com.preichert.cinemerick.feature.showtimes.domain.monthGrid
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_next_month
import com.preichert.cinemerick.feature.showtimes.presentation.resources.calendar_prev_month
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

// Keeps day cells compact on wide screens, where the calendar is shown inline.
private val MAX_CALENDAR_WIDTH = 320.dp

/**
 * Month calendar with multiple selection. Dates before [minDate] are disabled.
 * Material3's DatePicker only supports a single date or a contiguous range, hence this grid.
 */
@Composable
fun MultiDateCalendar(
    selected: Set<LocalDate>,
    minDate: LocalDate,
    onToggle: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var firstOfMonth by remember { mutableStateOf(LocalDate(minDate.year, minDate.month, 1)) }
    val minMonth = remember(minDate) { LocalDate(minDate.year, minDate.month, 1) }

    Column(modifier = modifier.fillMaxWidth().widthIn(max = MAX_CALENDAR_WIDTH), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { firstOfMonth = firstOfMonth.plus(-1, DateTimeUnit.MONTH) },
                enabled = firstOfMonth > minMonth
            ) {
                val description = stringResource(Res.string.calendar_prev_month)
                Text(
                    text = "‹",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { contentDescription = description }
                )
            }
            Text(
                text = "${firstOfMonth.month.italianName()} ${firstOfMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { firstOfMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH) }) {
                val description = stringResource(Res.string.calendar_next_month)
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { contentDescription = description }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { dow ->
                Text(
                    text = dow.italianName().take(1),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        val cells = monthGrid(firstOfMonth.year, firstOfMonth.month)
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { index ->
                    val date = week.getOrNull(index)
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                isSelected = date in selected,
                                isToday = date == minDate,
                                enabled = date >= minDate,
                                onToggle = { onToggle(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(if (isSelected) colors.primary else Color.Transparent)
            .then(if (isToday && !isSelected) Modifier.border(1.dp, colors.primary, CircleShape) else Modifier)
            .toggleable(value = isSelected, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() })
    ) {
        Text(
            text = date.day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isSelected -> colors.onPrimary
                enabled -> colors.onSurface
                else -> colors.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}
