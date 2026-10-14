package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_cast
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_budget
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_gallery
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_gallery_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_production
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_release_date
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_revenue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_countries
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_director
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_genre
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_language
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_none
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_original_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_rating
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_saga
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_score
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_score_value
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_synopsis
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_trailer
import kotlinx.datetime.LocalDate
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
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                details.backdropUrl?.let { Backdrop(it) }
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = film.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    details.tagline?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    details.metaLine()?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    details.synopsis?.let { DetailField(stringResource(Res.string.details_synopsis), it) }
                    details.genre?.let { DetailField(stringResource(Res.string.details_genre), it) }
                    details.saga?.let { DetailField(stringResource(Res.string.details_saga), it) }
                    details.originalTitleFor(film.title)?.let { DetailField(stringResource(Res.string.details_original_title), it) }
                    details.originalLanguage?.let { code ->
                        val name = remember(code) { languageDisplayName(code) } ?: languageCodeFallback(code)
                        DetailField(stringResource(Res.string.details_language), name)
                    }
                    details.countries?.let { DetailField(stringResource(Res.string.details_countries), it) }
                    details.releaseDate?.let { DetailField(stringResource(Res.string.details_release_date), formatReleaseDate(it)) }
                    details.productionCompanies?.let { DetailField(stringResource(Res.string.details_production), it) }
                    details.budget?.let { DetailField(stringResource(Res.string.details_budget), formatMoney(it)) }
                    details.revenue?.let { DetailField(stringResource(Res.string.details_revenue), formatMoney(it)) }
                    details.rating?.let { DetailField(stringResource(Res.string.details_rating), it) }
                    details.score?.let { score ->
                        DetailField(
                            stringResource(Res.string.details_score),
                            stringResource(Res.string.details_score_value, formatScore(score.value), score.votes.toString())
                        )
                    }
                    if (details.directors.isNotEmpty()) {
                        PeopleField(stringResource(Res.string.details_director), details.directors)
                    } else {
                        details.director?.let { DetailField(stringResource(Res.string.details_director), it) }
                    }
                    if (details.castMembers.isNotEmpty()) {
                        PeopleField(stringResource(Res.string.details_cast), details.castMembers)
                    } else {
                        details.cast?.let { DetailField(stringResource(Res.string.details_cast), it) }
                    }
                    if (details.gallery.isNotEmpty()) GalleryField(stringResource(Res.string.details_gallery), details.gallery)
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
                            LinkButton(stringResource(Res.string.details_trailer)) { uriHandler.openUri(url) }
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.details_close)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Text(text)
    }
}

/** 16:9 image above the title. Collapses completely if it cannot be loaded, so no empty area is left. */
@Composable
private fun Backdrop(url: String) {
    var failed by remember(url) { mutableStateOf(false) }
    if (failed) return
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        onError = { failed = true },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/** Thumbnails of the extra images; one that fails to load is left out, and the heading goes with the last of them. */
@Composable
private fun GalleryField(label: String, images: List<FilmImage>) {
    var failed by remember(images) { mutableStateOf(emptySet<String>()) }
    var opened by remember(images) { mutableStateOf<FilmImage?>(null) }
    val shown = images.filter { it.thumbUrl !in failed }
    if (shown.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Badge(
            text = label,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            bold = true
        )
        // A grid of three columns on every screen size; an incomplete last row keeps the width of the cells above.
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            shown.chunked(GALLERY_COLUMNS).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { image ->
                        AsyncImage(
                            model = image.thumbUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            onError = { failed = failed + image.thumbUrl },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(16f / 9f)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { opened = image }
                        )
                    }
                    repeat(GALLERY_COLUMNS - row.size) { Box(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }

    opened?.let { image ->
        val close = stringResource(Res.string.details_gallery_close)
        Dialog(onDismissRequest = { opened = null }) {
            AsyncImage(
                model = image.fullUrl,
                contentDescription = close,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { opened = null }
            )
        }
    }
}

private const val GALLERY_COLUMNS = 3

@Composable
private fun PeopleField(label: String, people: List<FilmPerson>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Badge(
            text = label,
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            bold = true
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            people.forEach { PersonCard(it) }
        }
    }
}

/** Photo, name and, for a cast member, the character. Without a photo the initial of the name stays visible. */
@Composable
private fun PersonCard(person: FilmPerson) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier.width(PERSON_WIDTH)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = person.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            person.photoUrl?.let {
                // Decorative: the name sits right below. A failed load leaves the initial showing.
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)
                )
            }
        }
        Text(
            text = person.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        person.role?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val PERSON_WIDTH = 72.dp

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
        score = FilmScore(6.8, 412),
        directors = listOf(FilmPerson(1, "Rob Savage")),
        castMembers = listOf(
            FilmPerson(2, "Alejandro Nieves", "Bela"),
            FilmPerson(3, "Jessica Chastain", "Una madre molto lontana dalla casa"),
            FilmPerson(4, "Senza Foto")
        ),
        tagline = "Non tutte le madri sono quello che sembrano",
        saga = "Mammina - Collezione",
        originalLanguage = "en",
        countries = "United States of America, Italy",
        productionCompanies = "Legendary Pictures, Blumhouse",
        budget = 190_000_000L,
        revenue = 714_844_358L,
        releaseDate = LocalDate(2026, 10, 14),
        gallery = List(4) { FilmImage("https://example.com/t$it.jpg", "https://example.com/f$it.jpg") }
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
private fun FilmDetailsOverlayTextOnlyPreview() {
    DarkThemePreview {
        FilmDetailsOverlay(
            FilmDetailsUi("Digger", FilmDetails(synopsis = "Trama", director = "Mario Rossi", cast = "Anna Bianchi, Luca Verdi", trailerUrl = "https://example.com")),
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun FilmDetailsOverlayEmptyPreview() {
    LightThemePreview { FilmDetailsOverlay(FilmDetailsUi("Digger", FilmDetails()), onDismiss = {}) }
}
