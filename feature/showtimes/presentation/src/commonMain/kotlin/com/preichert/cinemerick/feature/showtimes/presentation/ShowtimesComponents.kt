package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.CinemaBrandColors
import com.preichert.cinemerick.core.designsystem.CinemerickCard
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.shimmer
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import org.jetbrains.compose.resources.DrawableResource
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.generate_hint
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Display name of a chain, as printed on the chain card and the venue picker. */
internal fun Chain.displayName(): String = when (this) {
    Chain.THE_SPACE -> "The Space"
    Chain.UCI -> "UCI"
    Chain.NOTORIOUS -> "Notorious"
    Chain.CINERGIA -> "Cinergia"
    Chain.CRISTALLO -> "Cristallo"
    Chain.DON_BOSCO -> "Don Bosco"
    Chain.CINEMAZERO -> "Cinemazero"
    Chain.ARCADIA -> "Arcadia"
}

/** Two-letter badge text of a chain, as in the redesign's chain badges. */
internal fun Chain.shortName(): String = when (this) {
    Chain.THE_SPACE -> "TS"
    Chain.UCI -> "UCI"
    Chain.NOTORIOUS -> "NO"
    Chain.CINERGIA -> "CI"
    Chain.CRISTALLO -> "CR"
    Chain.DON_BOSCO -> "DB"
    Chain.CINEMAZERO -> "CZ"
    Chain.ARCADIA -> "AR"
}

/** Fill and on-color of a chain. Fixed in both themes: see CinemaBrandColors. */
internal fun Chain.badgeColors(): Pair<Color, Color> = when (this) {
    Chain.THE_SPACE -> CinemaBrandColors.theSpaceOrange to CinemaBrandColors.onTheSpaceOrange
    Chain.UCI -> CinemaBrandColors.uciBlue to CinemaBrandColors.onUciBlue
    Chain.NOTORIOUS -> CinemaBrandColors.notoriousSilver to CinemaBrandColors.onNotoriousSilver
    Chain.CINERGIA -> CinemaBrandColors.cinergia to CinemaBrandColors.onCinergia
    Chain.CRISTALLO -> CinemaBrandColors.cristallo to CinemaBrandColors.onCristallo
    Chain.DON_BOSCO -> CinemaBrandColors.donBosco to CinemaBrandColors.onDonBosco
    Chain.CINEMAZERO -> CinemaBrandColors.cinemazero to CinemaBrandColors.onCinemazero
    Chain.ARCADIA -> CinemaBrandColors.arcadia to CinemaBrandColors.onArcadia
}

@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    CinemerickCard(modifier = modifier, content = content)
}

/** Small status tag (format, sold out), shape xs, labelMedium. */
@Composable
internal fun Badge(text: String, container: Color, content: Color, bold: Boolean = false) {
    Surface(shape = MaterialTheme.shapes.extraSmall, color = container) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = Spacing.space2)
        )
    }
}

/** Empty and hint states: an outline icon and a line of text. No emoji. */
@Composable
internal fun MessageCard(icon: DrawableResource, text: String) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.space6),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.space2)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = CinemerickTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemerickTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun SkeletonCard() {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.space4),
            verticalArrangement = Arrangement.spacedBy(Spacing.space3)
        ) {
            SkeletonLine(widthFraction = 0.5f, height = 18.dp)
            SkeletonLine(widthFraction = 0.9f, height = 14.dp)
            SkeletonLine(widthFraction = 0.7f, height = 14.dp)
        }
    }
}

@Composable
internal fun SkeletonLine(widthFraction: Float, height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .shimmer()
    )
}

// Previews - Light theme

@Preview
@Composable
private fun SectionCardPreviewLight() {
    LightThemePreview {
        SectionCard {
            Text("Sample content inside card")
        }
    }
}

@Preview
@Composable
private fun BadgePreviewLight() {
    LightThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            Badge("2D", Color.Blue, Color.White)
            Badge("3D VO", Color.Green, Color.White, bold = true)
            Badge("sub ITA", Color.Red, Color.White)
        }
    }
}

@Preview
@Composable
private fun MessageCardPreviewLight() {
    LightThemePreview {
        MessageCard(icon = CinemerickIcons.Film, text = stringResource(Res.string.generate_hint))
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun MessageCardPreviewDark() {
    DarkThemePreview {
        MessageCard(icon = CinemerickIcons.Film, text = stringResource(Res.string.generate_hint))
    }
}
