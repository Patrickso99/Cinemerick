package com.preichert.cinemerick.feature.showtimes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
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
import com.preichert.cinemerick.core.designsystem.CinemerickTheme
import com.preichert.cinemerick.core.designsystem.DarkThemePreview
import com.preichert.cinemerick.core.designsystem.LightThemePreview
import com.preichert.cinemerick.core.designsystem.Spacing
import com.preichert.cinemerick.feature.showtimes.domain.FilmPerson
import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonFilm
import com.preichert.cinemerick.feature.showtimes.presentation.resources.Res
import com.preichert.cinemerick.feature.showtimes.presentation.resources.details_close
import com.preichert.cinemerick.feature.showtimes.presentation.resources.person_biography
import com.preichert.cinemerick.feature.showtimes.presentation.resources.person_birthday
import com.preichert.cinemerick.feature.showtimes.presentation.resources.person_birthplace
import com.preichert.cinemerick.feature.showtimes.presentation.resources.person_deathday
import com.preichert.cinemerick.feature.showtimes.presentation.resources.person_filmography
import com.preichert.cinemerick.feature.showtimes.presentation.resources.person_none
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Person details, opened from a cast or director avatar of the film sheet: a full-height sheet over a scrim, with the
 * photo beside the name, the facts TMDB has, the biography and the films the person worked on.
 */
@Composable
internal fun PersonDetailsOverlay(
    person: PersonUi,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PersonDetailsLayer(person = person, onDismiss = onDismiss)
    }
}

/** Scrim plus the sheet. Used by the dialog and by the previews. A tap on the scrim closes the sheet. */
@Composable
internal fun PersonDetailsLayer(
    person: PersonUi,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CinemerickTheme.colors.scrim)
                .clickable(role = Role.Button, onClick = onDismiss),
        )
        PersonDetailsSheet(
            person = person,
            onDismiss = onDismiss,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun PersonDetailsSheet(
    person: PersonUi,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CinemerickTheme.colors
    val details = person.details

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = colors.surfaceContainer,
        modifier = modifier
            .widthIn(max = 640.dp)
            .fillMaxHeight()
            .padding(top = 40.dp),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            PersonHeader(person = person, onDismiss = onDismiss)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.space4)
            ) {
                if (person.isLoading) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (details == null) {
                    Text(
                        text = stringResource(Res.string.person_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                } else {
                    details.birthday?.let { DetailField(stringResource(Res.string.person_birthday), formatReleaseDate(it)) }
                    details.deathday?.let { DetailField(stringResource(Res.string.person_deathday), formatReleaseDate(it)) }
                    details.placeOfBirth?.let { DetailField(stringResource(Res.string.person_birthplace), it) }
                    details.biography?.let { biography ->
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                            Text(
                                text = stringResource(Res.string.person_biography).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                            Synopsis(biography)
                        }
                    }
                    if (details.films.isNotEmpty()) FilmographyGrid(stringResource(Res.string.person_filmography), details.films)
                }
                Spacer(Modifier.height(Spacing.space8))
            }
        }
    }
}

/** Photo in a round frame, the name beside it, and the close button. Without a photo the initial shows instead. */
@Composable
private fun PersonHeader(person: PersonUi, onDismiss: () -> Unit) {
    val colors = CinemerickTheme.colors
    val closeLabel = stringResource(Res.string.details_close)
    val name = person.details?.name ?: person.person.name
    val photo = person.details?.photoUrl ?: person.person.photoUrl
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh),
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onSurfaceVariant,
                )
                photo?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.width(Spacing.space4))
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp),
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CinemerickIconButton(
            icon = painterResource(CinemerickIcons.Close),
            contentDescription = closeLabel,
            onClick = onDismiss,
            container = colors.surfaceContainerHigh,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        )
    }
}

private val FILMOGRAPHY_MIN_CARD = 120.dp

/**
 * The films in rows of as many posters as fit the width, each at least [FILMOGRAPHY_MIN_CARD] wide, most recent first. The
 * last row is left short, not stretched. Each card shows its year and the role played when TMDB has one.
 */
@Composable
private fun FilmographyGrid(label: String, films: List<PersonFilm>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = Spacing.space3
        val columns = ((maxWidth + gap) / (FILMOGRAPHY_MIN_CARD + gap)).toInt().coerceAtLeast(2)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = CinemerickTheme.colors.onSurfaceVariant,
            )
            films.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { film -> FilmCard(film, Modifier.weight(1f)) }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun FilmCard(film: PersonFilm, modifier: Modifier = Modifier) {
    val colors = CinemerickTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerHigh),
        ) {
            film.posterUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = film.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = film.title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitle = listOfNotNull(film.year?.toString(), film.role).joinToString(" · ")
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val previewPerson = PersonUi(
    person = FilmPerson(1, "Rob Savage"),
    details = PersonDetails(
        id = 1,
        name = "Rob Savage",
        biography = "Regista e sceneggiatore britannico, noto per i film dell'orrore a basso budget girati in pochi giorni.",
        birthday = kotlinx.datetime.LocalDate(1989, 5, 2),
        placeOfBirth = "Londra, Inghilterra, Regno Unito",
        films = listOf(
            PersonFilm(10, "Mammina", 2026, role = null, score = 6.8),
            PersonFilm(11, "Host", 2020, score = 7.1),
        ),
    ),
)

@Preview
@Composable
private fun PersonDetailsPreviewDark() {
    DarkThemePreview { PersonDetailsLayer(person = previewPerson, onDismiss = {}) }
}

@Preview
@Composable
private fun PersonDetailsPreviewLoadingLight() {
    LightThemePreview { PersonDetailsLayer(person = previewPerson.copy(details = null, isLoading = true), onDismiss = {}) }
}

@Preview
@Composable
private fun PersonDetailsPreviewNoneDark() {
    DarkThemePreview { PersonDetailsLayer(person = PersonUi(FilmPerson(2, "Senza Dettagli")), onDismiss = {}) }
}
