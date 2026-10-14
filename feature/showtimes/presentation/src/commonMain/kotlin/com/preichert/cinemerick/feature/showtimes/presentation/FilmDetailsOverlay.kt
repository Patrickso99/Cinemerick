package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_cast
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_director
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_genre
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_none
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_original_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_rating
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_score
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_score_value
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_synopsis
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_trailer
import org.jetbrains.compose.resources.stringResource

/** Modal sheet over the dimmed results. Fields a film has no value for are left out. */
@Composable
internal fun FilmDetailsOverlay(
    film: FilmDetailsUi,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val details = film.details

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Same look as the film cards of the results.
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .padding(Spacing.lg)
                .widthIn(max = 560.dp)
                .heightIn(max = 640.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                details.metaLine()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                details.synopsis?.let { DetailField(stringResource(Res.string.details_synopsis), it) }
                details.genre?.let { DetailField(stringResource(Res.string.details_genre), it) }
                details.director?.let { DetailField(stringResource(Res.string.details_director), it) }
                details.cast?.let { DetailField(stringResource(Res.string.details_cast), it) }
                details.rating?.let { DetailField(stringResource(Res.string.details_rating), it) }
                details.originalTitleFor(film.title)?.let { DetailField(stringResource(Res.string.details_original_title), it) }
                details.score?.let { score ->
                    DetailField(
                        stringResource(Res.string.details_score),
                        stringResource(Res.string.details_score_value, formatScore(score.value), score.votes.toString())
                    )
                }
                if (film.isLoading) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (details.isEmpty) {
                    Text(
                        text = stringResource(Res.string.details_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    details.trailerUrl?.let { url ->
                        Button(
                            onClick = { uriHandler.openUri(url) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(stringResource(Res.string.details_trailer))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                        TextButton(onClick = onDismiss) { Text(stringResource(Res.string.details_close)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Badge(
            text = label,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            bold = true
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private val previewFilm = FilmDetailsUi(
    title = "Mammina",
    details = FilmDetails(
        synopsis = "Un viaggio terrificante nel mondo sempre più oscuro di Bela.",
        genre = "Horror",
        director = "Rob Savage",
        cast = "Alejandro Nieves, Jessica Chastain",
        rating = "14+ · Violenza, Armi",
        trailerUrl = "https://www.youtube.com/watch?v=abc",
        runtimeMinutes = 101,
        year = 2026,
        originalTitle = "Mother",
        score = FilmScore(6.8, 412)
    )
)

@Preview
@Composable
private fun FilmDetailsOverlayPreviewLight() {
    LightThemePreview { FilmDetailsOverlay(previewFilm, onDismiss = {}) }
}

@Preview
@Composable
private fun FilmDetailsOverlayPreviewDark() {
    DarkThemePreview { FilmDetailsOverlay(previewFilm.copy(details = FilmDetails(genre = "Drama"), isLoading = true), onDismiss = {}) }
}

@Preview
@Composable
private fun FilmDetailsOverlayEmptyPreview() {
    LightThemePreview { FilmDetailsOverlay(FilmDetailsUi("Digger", FilmDetails()), onDismiss = {}) }
}
