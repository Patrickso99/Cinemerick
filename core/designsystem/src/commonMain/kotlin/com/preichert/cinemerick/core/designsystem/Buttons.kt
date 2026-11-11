package com.preichert.cinemerick.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val interaction = remember { MutableInteractionSource() }
    // Disabled keeps the filled look and dims the whole button, as before the M3 base.
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .pressScale(interaction)
            .alpha(if (enabled) 1f else 0.5f),
        interactionSource = interaction,
        shape = CinemerickFullShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary,
            disabledContentColor = colors.onPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 22.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
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
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .pressScale(interaction)
            .alpha(if (enabled) 1f else 0.5f),
        interactionSource = interaction,
        shape = CinemerickFullShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.onSurface,
            disabledContentColor = colors.onSurface,
        ),
        border = BorderStroke(1.dp, colors.outlineVariant),
        contentPadding = PaddingValues(horizontal = 22.dp),
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
    val interaction = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .pressScale(interaction),
        interactionSource = interaction,
        shape = CinemerickFullShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer, maxLines = 1)
            if (icon != null) Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(18.dp))
        }
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
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = container, contentColor = tint),
    ) {
        // The description belongs to the icon, so the button's accessible name is read from the icon itself.
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
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
