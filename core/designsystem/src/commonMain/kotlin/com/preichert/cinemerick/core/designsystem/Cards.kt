package com.preichert.cinemerick.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

/** Card: surface, shape lg, tonal depth only, no outline or shadow. */
@Composable
fun CinemerickCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = CinemerickTheme.colors.surfaceContainer),
    ) {
        Column(content = content)
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
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
