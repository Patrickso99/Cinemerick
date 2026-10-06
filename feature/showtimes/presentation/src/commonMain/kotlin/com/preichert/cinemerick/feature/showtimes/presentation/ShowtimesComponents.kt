package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.core.designsystem.CinemaBrandColors
import com.preichert.cinemerick.core.designsystem.shimmer
import com.preichert.cinemerick.core.designsystem.DevicePreviews
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.theme_dark
import org.jetbrains.compose.resources.stringResource

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
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        content = content
    )
}

@Composable
internal fun Badge(text: String, container: Color, content: Color, bold: Boolean = false) {
    Surface(shape = MaterialTheme.shapes.small, color = container) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (bold) androidx.compose.ui.text.font.FontWeight.Bold else null,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}

@Composable
internal fun MessageCard(emoji: String, text: String) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(emoji, style = MaterialTheme.typography.displaySmall)
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
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

@Composable
internal fun ThemeSwitch(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val description = stringResource(Res.string.theme_dark)
    Switch(
        checked = darkTheme,
        onCheckedChange = onDarkThemeChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color.White.copy(alpha = 0.45f),
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
            uncheckedBorderColor = Color.White.copy(alpha = 0.7f),
        ),
        modifier = Modifier.semantics { contentDescription = description }
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
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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
        MessageCard(emoji = "🎬", text = "Generate showtimes to see results")
    }
}

@Preview
@Composable
private fun SkeletonCardPreviewLight() {
    LightThemePreview {
        SkeletonCard()
    }
}

@Preview
@Composable
private fun ThemeSwitchPreviewLight() {
    LightThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text("Dark theme:", style = MaterialTheme.typography.labelMedium)
            ThemeSwitch(darkTheme = false, onDarkThemeChange = {})
        }
    }
}

// Previews - Dark theme

@Preview
@Composable
private fun SectionCardPreviewDark() {
    DarkThemePreview {
        SectionCard {
            Text("Sample content inside card")
        }
    }
}

@Preview
@Composable
private fun BadgePreviewDark() {
    DarkThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Badge("2D", Color.Blue, Color.White)
            Badge("3D VO", Color.Green, Color.White, bold = true)
            Badge("sub ITA", Color.Red, Color.White)
        }
    }
}

@Preview
@Composable
private fun MessageCardPreviewDark() {
    DarkThemePreview {
        MessageCard(emoji = "🎬", text = "Generate showtimes to see results")
    }
}

@Preview
@Composable
private fun SkeletonCardPreviewDark() {
    DarkThemePreview {
        SkeletonCard()
    }
}

@Preview
@Composable
private fun ThemeSwitchPreviewDark() {
    DarkThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text("Dark theme:", style = MaterialTheme.typography.labelMedium)
            ThemeSwitch(darkTheme = true, onDarkThemeChange = {})
        }
    }
}
