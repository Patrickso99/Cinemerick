package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.preichert.cinemerick.core.designsystem.CinemerickIconButton
import com.preichert.cinemerick.core.designsystem.CinemerickIcons
import com.preichert.cinemerick.core.designsystem.CinemerickPrimaryButton
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmImage
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.FilmScore
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_budget
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_cast
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_countries
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_director
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_gallery
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_gallery_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_language
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_none
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_original_title
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_production
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_rating
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_read_more
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_release_date
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_revenue
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_saga
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_score
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_score_value
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_trailer
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Film details, as in docs/design (Phone, 04): a full-height sheet over a scrim, with a 40dp gap at the top. The
 * backdrop fades into the surface, the poster overlaps it, the title sits beside the poster. Then one primary
 * action (trailer), genres, synopsis with "Read more", cast, gallery and every fact the film has.
 */
@Composable
internal fun FilmDetailsOverlay(
    film: FilmDetailsUi,
    onDismiss: () -> Unit,
    onPersonClick: (FilmPerson) -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        FilmDetailsLayer(film = film, onDismiss = onDismiss, onPersonClick = onPersonClick)
    }
}

/** Scrim plus the sheet. Used by the dialog and by the previews. A tap on the scrim closes the sheet. */
@Composable
internal fun FilmDetailsLayer(
    film: FilmDetailsUi,
    onDismiss: () -> Unit,
    onPersonClick: (FilmPerson) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CinemerickTheme.colors.scrim)
                .clickable(role = Role.Button, onClick = onDismiss),
        )
        FilmDetailsSheet(
            film = film,
            onDismiss = onDismiss,
            onPersonClick = onPersonClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun FilmDetailsSheet(
    film: FilmDetailsUi,
    onDismiss: () -> Unit,
    onPersonClick: (FilmPerson) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val details = film.details
    val colors = CinemerickTheme.colors

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = colors.surfaceContainer,
        modifier = modifier
            .widthIn(max = 640.dp)
            .fillMaxHeight()
            .padding(top = 40.dp),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            FilmHeader(film = film, onDismiss = onDismiss)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.space4)
            ) {
                details.tagline?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = colors.onSurfaceVariant
                    )
                }
                details.trailerUrl?.let { url ->
                    CinemerickPrimaryButton(
                        text = stringResource(Res.string.details_trailer),
                        onClick = { uriHandler.openUri(url) },
                        icon = painterResource(CinemerickIcons.Play),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                details.genre?.let { GenreTags(it) }
                details.synopsis?.let { Synopsis(it) }
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
                    PeopleRow(stringResource(Res.string.details_director), details.directors, onPersonClick)
                } else {
                    details.director?.let { DetailField(stringResource(Res.string.details_director), it) }
                }
                if (details.castMembers.isNotEmpty()) {
                    PeopleRow(stringResource(Res.string.details_cast), details.castMembers, onPersonClick)
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
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Spacing.space8))
            }
        }
    }
}

/**
 * Backdrop (236dp, fading to the surface) with the close button, and the poster overlapping it at the left, the
 * title and year beside the poster. Without a poster, the title sits under the backdrop.
 */
@Composable
private fun FilmHeader(film: FilmDetailsUi, onDismiss: () -> Unit) {
    val colors = CinemerickTheme.colors
    val details = film.details
    val titleStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp)
    val closeLabel = stringResource(Res.string.details_close)
    Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(236.dp).background(colors.surfaceContainerHigh)) {
            details.backdropUrl?.let { Backdrop(it) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(0f to Color.Transparent, 0.35f to Color.Transparent, 1f to colors.surfaceContainer),
                        )
                    )
            )
        }
        CinemerickIconButton(
            icon = painterResource(CinemerickIcons.Close),
            contentDescription = closeLabel,
            onClick = onDismiss,
            container = Color(0x8C0A0812),
            tint = Color.White,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 150.dp, end = 20.dp),
            verticalAlignment = Alignment.Top,
        ) {
            film.posterUrl?.let { url ->
                Box(
                    modifier = Modifier
                        .width(112.dp)
                        .aspectRatio(2f / 3f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.surfaceContainerHigh)
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = film.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(Modifier.width(Spacing.space4))
            }
            Column(modifier = Modifier.padding(top = if (film.posterUrl != null) 90.dp else 0.dp).weight(1f, fill = false)) {
                TitleOrLogo(film = film, style = titleStyle)
                val meta = details.metaLine()
                if (meta == null && details.score == null) return
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                    meta?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    details.score?.let { score ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(
                                painter = painterResource(CinemerickIcons.Star),
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = formatScore(score.value),
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The title as text, or as TMDB's logo when TMDB has one, tinted to the surface so it reads in both themes. The text is
 * kept when the logo cannot be loaded.
 */
@Composable
private fun TitleOrLogo(film: FilmDetailsUi, style: TextStyle) {
    val colors = CinemerickTheme.colors
    var logoFailed by remember(film.details.logoUrl) { mutableStateOf(false) }
    val logo = film.details.logoUrl?.takeUnless { logoFailed }
    if (logo != null) {
        AsyncImage(
            model = logo,
            contentDescription = film.title,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(colors.onSurface),
            onError = { logoFailed = true },
            modifier = Modifier.height(64.dp).widthIn(max = 220.dp),
        )
    } else {
        Text(
            text = film.title,
            style = style,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Backdrop image. Collapses to nothing if it cannot be loaded, so the gradient alone stays. */
@Composable
private fun Backdrop(url: String) {
    var failed by remember(url) { mutableStateOf(false) }
    if (failed) return
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        onError = { failed = true },
        modifier = Modifier.fillMaxSize()
    )
}

/** Genres as format-style tags, shape xs, surfaceContainerHigh. */
@Composable
private fun GenreTags(genres: String) {
    val colors = CinemerickTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { genre ->
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(colors.surfaceContainerHigh)
                    .padding(horizontal = Spacing.space3),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = genre, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** Synopsis, three lines; "Read more" appears only when the text is longer and opens the rest. */
@Composable
internal fun Synopsis(text: String) {
    var expanded by remember(text) { mutableStateOf(false) }
    var overflows by remember(text) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = CinemerickTheme.colors.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow },
        )
        if (overflows && !expanded) {
            Text(
                text = stringResource(Res.string.details_read_more),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable(role = Role.Button) { expanded = true }
                    .padding(vertical = 14.dp),
            )
        }
    }
}

/** Gallery as a horizontal carousel of 172x98 frames; a frame that fails to load is left out. */
@Composable
private fun GalleryField(label: String, images: List<FilmImage>) {
    var failed by remember(images) { mutableStateOf(emptySet<String>()) }
    var opened by remember(images) { mutableStateOf<FilmImage?>(null) }
    val shown = images.filter { it.thumbUrl !in failed }
    if (shown.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = CinemerickTheme.colors.onSurfaceVariant,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            items(shown, key = { it.thumbUrl }) { image ->
                AsyncImage(
                    model = image.thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = { failed = failed + image.thumbUrl },
                    modifier = Modifier
                        .size(width = 172.dp, height = 98.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(CinemerickTheme.colors.surfaceContainerHigh)
                        .clickable { opened = image }
                )
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

/** Cast or directors as a horizontal row of round avatars with the name and, for cast, the character. */
@Composable
private fun PeopleRow(label: String, people: List<FilmPerson>, onPersonClick: (FilmPerson) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = CinemerickTheme.colors.onSurfaceVariant,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            items(people, key = { it.id }) { person -> PersonAvatar(person, onClick = { onPersonClick(person) }) }
        }
    }
}

/** 64dp round photo, the initial of the name behind it. A failed load leaves the initial showing. */
@Composable
private fun PersonAvatar(person: FilmPerson, onClick: () -> Unit) {
    val colors = CinemerickTheme.colors
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space1),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh),
        ) {
            Text(
                text = person.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurfaceVariant,
            )
            person.photoUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = person.name,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        person.role?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = CinemerickTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = CinemerickTheme.colors.onSurface
        )
    }
}

private val previewFilm = FilmDetailsUi(
    title = "Mammina",
    details = FilmDetails(
        synopsis = "Un viaggio terrificante nel mondo sempre più oscuro di Bela. Un thriller che parla di maternità, segreti di famiglia e di una casa che non lascia andare nessuno, nemmeno chi se ne vuole andare davvero.",
        genre = "Horror, Thriller",
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
    ),
    posterUrl = "https://example.com/poster.jpg",
)

@Preview
@Composable
private fun FilmDetailsPreviewLight() {
    LightThemePreview { FilmDetailsLayer(film = previewFilm, onDismiss = {}) }
}

@Preview
@Composable
private fun FilmDetailsPreviewDark() {
    DarkThemePreview { FilmDetailsLayer(film = previewFilm, onDismiss = {}) }
}

@Preview
@Composable
private fun FilmDetailsPreviewLoadingDark() {
    DarkThemePreview { FilmDetailsLayer(film = previewFilm.copy(details = FilmDetails(genre = "Drama"), isLoading = true), onDismiss = {}) }
}

@Preview
@Composable
private fun FilmDetailsPreviewTextOnlyLight() {
    LightThemePreview {
        FilmDetailsLayer(
            FilmDetailsUi("Digger", FilmDetails(synopsis = "Trama", director = "Mario Rossi", cast = "Anna Bianchi, Luca Verdi", trailerUrl = "https://example.com")),
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun FilmDetailsPreviewEmptyDark() {
    DarkThemePreview { FilmDetailsLayer(FilmDetailsUi("Digger", FilmDetails()), onDismiss = {}) }
}
