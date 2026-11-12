package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequest
import com.preichert.cinemerick.feature.showtimes.domain.TimeSpan
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.preichert.cinemerick.feature.showtimes.domain.VenueMatch
import com.preichert.cinemerick.feature.showtimes.domain.hiddenTagsFor
import com.preichert.cinemerick.feature.showtimes.domain.matchVenue
import kotlinx.datetime.LocalDate

/**
 * Turns a request into the review the user checks before searching. Cinemas are matched against [catalog] (the venues
 * of each chain that loaded), days get the window the user said or the default one ([newDay]), and a window said
 * without days applies to the [selectedDays].
 */
internal fun buildVoiceReview(
    request: CinemaRequest,
    catalog: Map<Chain, List<Venue>>,
    selectedVenues: Set<Venue>,
    selectedDays: List<DayUi>,
    knownTags: Set<String>,
    newDay: (LocalDate) -> DayUi,
): VoiceReviewUi {
    val certain = mutableListOf<Venue>()
    val questions = mutableListOf<VenueQuestionUi>()
    val notFound = mutableListOf<String>()
    request.venues.forEach { mention ->
        when (val match = matchVenue(mention, catalog, preferred = selectedVenues)) {
            is VenueMatch.Certain -> certain += match.venue
            is VenueMatch.Close -> questions += VenueQuestionUi(mention.spoken, match.best, match.candidates)
            VenueMatch.NotFound -> notFound += mention.spoken
        }
    }
    // A cinema named twice ("UCI Marcon… sì, UCI Marcon") is listed once; a question about a cinema already certain is dropped.
    val venues = certain.distinct()
    val openQuestions = questions.filterNot { it.best in venues }.distinctBy { it.best }

    val days = when {
        request.days.isNotEmpty() -> request.days.map { day -> newDay(day.date).withTime(day.time) }
        request.time != null -> selectedDays.filter { it.isSelected }.map { it.withTime(request.time) }.ifEmpty { null }
        else -> null
    }

    val formats = if (request.onlyFormats.isEmpty() && request.excludedFormats.isEmpty()) {
        null
    } else {
        FormatChoiceUi(
            only = request.onlyFormats.sorted(),
            excluded = request.excludedFormats.sorted(),
            hiddenTags = hiddenTagsFor(request.onlyFormats, request.excludedFormats, knownTags),
        )
    }

    return VoiceReviewUi(
        venues = if (request.venues.isEmpty()) null else venues,
        questions = openQuestions,
        notFound = notFound.distinct(),
        days = days,
        formats = formats,
        films = request.films.ifEmpty { null },
    )
}

// A bound the user did not say keeps the day's own (the default window for a new day).
private fun DayUi.withTime(time: TimeSpan?): DayUi {
    if (time == null) return this
    return copy(minTime = time.from ?: minTime, maxTime = time.to ?: maxTime)
}

/** The review after the user confirmed [venue] for the question about [spoken]: it joins the cinemas. */
internal fun VoiceReviewUi.confirm(spoken: String, venue: Venue): VoiceReviewUi = copy(
    venues = ((venues ?: emptyList()) + venue).distinct(),
    questions = questions.filterNot { it.spoken == spoken },
)

/** The review without the question about [spoken]: that cinema is skipped. */
internal fun VoiceReviewUi.ignore(spoken: String): VoiceReviewUi = copy(questions = questions.filterNot { it.spoken == spoken })

internal fun VoiceReviewUi.showCandidates(spoken: String): VoiceReviewUi =
    copy(questions = questions.map { if (it.spoken == spoken) it.copy(showCandidates = true) else it })

internal fun VoiceReviewUi.removeVenue(venue: Venue): VoiceReviewUi = copy(venues = venues?.minus(venue))

internal fun VoiceReviewUi.removeFilm(title: String): VoiceReviewUi = copy(films = films?.minus(title)?.ifEmpty { null })

internal fun VoiceReviewUi.removeDay(date: LocalDate): VoiceReviewUi = copy(days = days?.filterNot { it.date == date }?.ifEmpty { null })

internal fun VoiceReviewUi.removeFormats(): VoiceReviewUi = copy(formats = null)

/**
 * The filters after the search: each mentioned group replaces the one on screen. Cinemas mentioned but none found
 * leave the selection alone, so a request never empties it by mistake; open questions are skipped.
 */
internal fun ShowtimesState.withVoiceReview(review: VoiceReviewUi): ShowtimesState = copy(
    selectedVenues = review.venues?.takeIf { it.isNotEmpty() }?.toSet() ?: selectedVenues,
    days = review.days ?: days,
    hiddenTags = review.formats?.hiddenTags ?: hiddenTags,
    filmFilter = review.films?.joinToString(", ") ?: filmFilter,
)
