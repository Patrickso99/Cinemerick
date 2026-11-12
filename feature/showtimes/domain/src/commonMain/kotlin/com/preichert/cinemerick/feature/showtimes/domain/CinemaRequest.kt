package com.preichert.cinemerick.feature.showtimes.domain

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import kotlinx.datetime.LocalDate

/**
 * What the user asked the assistant for, as the assistant understood it. A group the user did not mention is empty
 * (or null): only the mentioned groups replace the filters on screen.
 */
data class CinemaRequest(
    val venues: List<VenueMention> = emptyList(),
    val days: List<DayRequest> = emptyList(),
    /** A time window said without any day ("dopo le 20"): it applies to the days already selected. */
    val time: TimeSpan? = null,
    val onlyFormats: Set<String> = emptySet(),
    val excludedFormats: Set<String> = emptySet(),
    val films: List<String> = emptyList(),
) {
    val isEmpty: Boolean
        get() = venues.isEmpty() && days.isEmpty() && time == null && onlyFormats.isEmpty() && excludedFormats.isEmpty() && films.isEmpty()
}

/** A cinema as the user said it ([spoken]), with the [chain] and [place] the assistant read in it. */
data class VenueMention(
    val spoken: String,
    val chain: Chain? = null,
    val place: String? = null,
)

data class DayRequest(val date: LocalDate, val time: TimeSpan? = null)

/** Time bounds as typed in the time fields ("17:00", "24:00"); null is no bound. */
data class TimeSpan(val from: String? = null, val to: String? = null)

/** Turns what the user said or typed into a [CinemaRequest], using the assistant the user set up. */
interface CinemaRequestInterpreter {
    suspend fun interpret(text: String, today: LocalDate): Result<CinemaRequest, AssistantError>
}
