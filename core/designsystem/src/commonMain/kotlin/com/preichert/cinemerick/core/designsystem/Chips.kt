package com.preichert.cinemerick.core.designsystem

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
