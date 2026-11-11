package com.preichert.cinemerick.core.designsystem

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import kotlin.math.roundToInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Two-option (or three) segmented control, 48dp. Used for theme and language. */
@Composable
fun CinemerickSegmentedControl(
    options: List<Pair<String, Painter?>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CinemerickFullShape)
            .background(colors.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, (label, icon) ->
            val selected = index == selectedIndex
            val container by animateColorAsState(
                if (selected) colors.primaryContainer else Color.Transparent,
                tween(Motion.duration(LocalAnimationsEnabled.current, Motion.Press)),
                label = "segmentContainer",
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CinemerickFullShape)
                    .background(container)
                    .semantics { this.selected = selected }
                    .clickable(role = Role.RadioButton, onClick = { onSelect(index) }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Modal bottom sheet with the redesign shape (xl on the top corners), handle and scrim. No blur. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemerickBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CinemerickTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surfaceContainer,
        scrimColor = colors.scrim,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.onSurfaceVariant.copy(alpha = 0.5f)),
            )
        },
    ) {
        Column(content = content)
    }
}

/** Section label above a settings group: labelSmall caps. */
@Composable
fun CinemerickSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = CinemerickTheme.colors.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

/**
 * Settings row. The whole row is the touch target. [trailing] is the value, control or chevron.
 */
@Composable
fun CinemerickSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

/** Switch row: the whole row toggles, the switch is 52x32 with a primary track when on. */
@Composable
fun CinemerickSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    val colors = CinemerickTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(role = Role.Switch, onClick = { onCheckedChange(!checked) })
            .semantics { this.selected = checked }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        CinemerickSwitch(checked = checked)
    }
}

/** Visual switch, 52x32, track primary when on. Pointer handling is on the row. */
@Composable
fun CinemerickSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = CinemerickTheme.colors
    // onCheckedChange is null: the row owns the pointer, the switch only shows the state.
    Switch(
        checked = checked,
        onCheckedChange = null,
        modifier = modifier.size(width = 52.dp, height = 32.dp),
        // Without thumb content M3 draws the unchecked thumb at 16dp; the design keeps 24dp in both states.
        thumbContent = { Spacer(Modifier.size(24.dp)) },
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = colors.onSurfaceVariant,
            uncheckedTrackColor = colors.surfaceContainerHigh,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}

/** Chip-sized text used for the time value on rows: tabular numerals, time style. */
@Composable
fun CinemerickTimeText(text: String, modifier: Modifier = Modifier, color: Color = CinemerickTheme.colors.onSurface) {
    Text(
        text = text,
        style = cinemerickTimeStyle(),
        color = color,
        modifier = modifier,
        maxLines = 1,
    )
}

/** End of the day in minutes: the end of the time window slider, which also means "no limit" in Settings. */
const val TIME_WINDOW_MAX_MINUTES = 1440

/**
 * Time window slider, the same on Home (per day) and in Settings (default): 00:00 to 24:00 in 15-minute steps.
 * [onChange] receives the new start and end in minutes.
 */
@Composable
fun CinemerickTimeRangeSlider(
    startMinutes: Int,
    endMinutes: Int,
    onChange: (startMinutes: Int, endMinutes: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    RangeSlider(
        value = startMinutes.toFloat()..endMinutes.toFloat(),
        onValueChange = { range -> onChange(range.start.roundToInt(), range.endInclusive.roundToInt()) },
        valueRange = 0f..TIME_WINDOW_MAX_MINUTES.toFloat(),
        steps = TIME_WINDOW_MAX_MINUTES / 15 - 1,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = colors.primary,
            activeTrackColor = colors.primary,
            inactiveTrackColor = colors.surfaceContainerHigh,
        ),
    )
}
