package com.preichert.cinemerick.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import kotlin.math.roundToInt
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Card: surface, shape lg, tonal depth only, no outline or shadow. */
@Composable
fun CinemerickCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = CinemerickTheme.colors.surfaceContainer,
    ) {
        Column(content = content)
    }
}

/**
 * Filter chip for formats, days and venues. 40dp visual inside a 48dp hit area, shape sm.
 * Selected = primaryContainer plus a check icon, so the state never depends on color alone.
 */
@Composable
fun CinemerickFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: DrawableResource? = null,
    leadingDot: Color? = null,
    enabled: Boolean = true,
) {
    val colors = CinemerickTheme.colors
    val container by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surfaceContainerHigh,
        tween(Motion.duration(LocalAnimationsEnabled.current, Motion.Press)),
        label = "filterChipContainer",
    )
    val content = if (selected) colors.onPrimaryContainer else colors.onSurface
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics { this.selected = selected }
            .clickable(
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(container)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
        ) {
            val iconRes = leadingIcon ?: if (selected) CinemerickIcons.Check else null
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (leadingDot != null) {
                Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(leadingDot))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Cinema chip for results: 28dp, shape xs, labelMedium. Fill and text are the CinemaBrandColors pair.
 * Dark theme adds the chainEdge stroke so the dark chains stay visible on the dark ground.
 */
@Composable
fun CinemerickChainChip(
    label: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(container)
            .border(1.dp, CinemerickTheme.colors.chainEdge, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Chain badge: 40dp, shape sm, initials in the chain colors. */
@Composable
fun CinemerickChainBadge(
    initials: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .border(1.dp, CinemerickTheme.colors.chainEdge, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
        )
    }
}

/**
 * One cinema chain as a summary card: badge, name, venues selected, and the venue chips when expanded.
 * Tap anywhere on the header to expand or collapse. The state lives in the caller, one flag per chain.
 */
@Composable
fun CinemerickChainCard(
    name: String,
    subtitle: String,
    initials: String,
    badgeContainer: Color,
    badgeContent: Color,
    selectedCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onQuickAdd: (() -> Unit)? = null,
    venues: @Composable () -> Unit,
) {
    val colors = CinemerickTheme.colors
    val animationsEnabled = LocalAnimationsEnabled.current
    val trailingIcon = when {
        expanded -> CinemerickIcons.ChevronUp
        selectedCount == 0 -> CinemerickIcons.Plus
        else -> CinemerickIcons.ChevronRight
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainer,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = name,
                        onClick = onToggle,
                    )
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
            ) {
                CinemerickChainBadge(
                    initials = initials,
                    container = badgeContainer,
                    content = badgeContent,
                    modifier = Modifier.align(Alignment.Top),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                if (selectedCount > 0) {
                    CountPill(selectedCount)
                }
                // An empty, collapsed chain shows a plus that opens the venue picker directly (quick add).
                val quickAdd = onQuickAdd.takeIf { !expanded && selectedCount == 0 }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .then(if (quickAdd != null) Modifier.clickable(role = Role.Button, onClickLabel = name, onClick = quickAdd) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(trailingIcon),
                        contentDescription = if (quickAdd != null) name else null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = if (animationsEnabled) expandVertically(tween(Motion.Sheet)) + fadeIn(tween(Motion.ListEnter)) else androidx.compose.animation.EnterTransition.None,
                exit = if (animationsEnabled) shrinkVertically(tween(Motion.Sheet)) + fadeOut(tween(Motion.ListEnter)) else androidx.compose.animation.ExitTransition.None,
            ) {
                Box(Modifier.padding(start = 64.dp, end = 14.dp, bottom = 14.dp)) {
                    venues()
                }
            }
        }
    }
}

/** Selected-venue count pill on a chain card. */
@Composable
private fun CountPill(count: Int) {
    val colors = CinemerickTheme.colors
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 28.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(colors.primaryContainer)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onPrimaryContainer,
        )
    }
}

/** Primary action of a screen: primary fill, pill shape, 52dp. Only one per screen. */
@Composable
fun CinemerickPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: Painter? = null,
) {
    val colors = CinemerickTheme.colors
    ButtonShell(
        onClick = onClick,
        enabled = enabled,
        height = 52.dp,
        container = colors.primary,
        modifier = modifier,
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.onPrimary,
            )
            icon != null -> Icon(icon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall, color = colors.onPrimary, maxLines = 1)
    }
}

/** Secondary action, e.g. Clear: transparent with a 1dp outlineVariant border. */
@Composable
fun CinemerickSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = CinemerickTheme.colors
    ButtonShell(
        onClick = onClick,
        enabled = enabled,
        height = 52.dp,
        container = Color.Transparent,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1)
    }
}

/** Tonal action, 44dp: "Open details". */
@Composable
fun CinemerickTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    val colors = CinemerickTheme.colors
    ButtonShell(
        onClick = onClick,
        enabled = true,
        height = 44.dp,
        container = colors.primaryContainer,
        modifier = modifier,
        horizontalPadding = 16.dp,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer, maxLines = 1)
        if (icon != null) Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(18.dp))
    }
}

/** Floating action on Results (Copy poll): primary fill, 56dp, with the L2 shadow. */
@Composable
fun CinemerickExtendedFab(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    ButtonShell(
        onClick = onClick,
        enabled = true,
        height = 56.dp,
        container = colors.primary,
        modifier = modifier.shadow(12.dp, CinemerickFullShape, ambientColor = Color.Black.copy(alpha = 0.5f), spotColor = Color.Black.copy(alpha = 0.5f)),
        horizontalPadding = 22.dp,
    ) {
        Icon(icon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(22.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, color = colors.onPrimary, maxLines = 1)
    }
}

/** Circular icon button: 48dp hit, 22dp outline icon, always with a content description. */
@Composable
fun CinemerickIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    container: Color = CinemerickTheme.colors.surfaceContainerHigh,
    tint: Color = CinemerickTheme.colors.onSurface,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .semantics { this.contentDescription = contentDescription }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

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
    val animationsEnabled = LocalAnimationsEnabled.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(Motion.duration(animationsEnabled, Motion.Press)),
        label = "switchThumb",
    )
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(CinemerickFullShape)
            .background(if (checked) colors.primary else colors.surfaceContainerHigh)
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) colors.onPrimary else colors.onSurfaceVariant),
        )
    }
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

/** Shared shape for the pill buttons: full shape, min tap target 48dp, press scale 0.97. */
@Composable
private fun ButtonShell(
    onClick: () -> Unit,
    enabled: Boolean,
    height: Dp,
    container: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    horizontalPadding: Dp = 22.dp,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .height(height)
            .pressScale(interaction)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(CinemerickFullShape)
            .background(container)
            .then(if (border != null) Modifier.border(border, CinemerickFullShape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        content()
    }
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
