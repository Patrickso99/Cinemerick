package com.preichert.cinemerick.feature.showtimes.data

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.AssistantRepository
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequest
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequestInterpreter
import com.preichert.cinemerick.feature.showtimes.domain.DayRequest
import com.preichert.cinemerick.feature.showtimes.domain.TimeSpan
import com.preichert.cinemerick.feature.showtimes.domain.VenueMention
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val log = Logger.withTag("LlmCinemaRequestInterpreter")

// Days the assistant can pick from: today and the next two weeks.
private const val CALENDAR_DAYS = 15
private val timePattern = Regex("""^([01]?\d|2[0-4]):[0-5]\d$""")

/**
 * Asks the user's assistant to read the request as JSON, then keeps only what makes sense: dates from the calendar it
 * was given, times written as the time fields expect, chains the app knows.
 */
class LlmCinemaRequestInterpreter(private val assistant: AssistantRepository) : CinemaRequestInterpreter {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun interpret(text: String, today: LocalDate): Result<CinemaRequest, AssistantError> {
        val calendar = (0 until CALENDAR_DAYS).map { today.plus(it, DateTimeUnit.DAY) }
        return when (val answer = assistant.complete(systemPrompt(calendar), text)) {
            is Result.Failure -> answer
            is Result.Success -> parse(answer.data, calendar.toSet())
                ?.let { Result.Success(it) }
                ?: Result.Failure(AssistantError.BAD_ANSWER)
        }
    }

    // Models sometimes wrap the object in a code fence or a sentence: only the outermost braces are read.
    internal fun parse(answer: String, calendar: Set<LocalDate>): CinemaRequest? {
        val start = answer.indexOf('{')
        val end = answer.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val dto = try {
            json.decodeFromString<CinemaRequestDto>(answer.substring(start, end + 1))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.w(e) { "Unreadable request" }
            return null
        }
        return dto.toCinemaRequest(calendar)
    }
}

@Serializable
internal data class CinemaRequestDto(
    val venues: List<VenueMentionDto> = emptyList(),
    val days: List<DayRequestDto> = emptyList(),
    val time: TimeSpanDto? = null,
    val onlyFormats: List<String> = emptyList(),
    val excludedFormats: List<String> = emptyList(),
    val films: List<String> = emptyList(),
)

@Serializable
internal data class VenueMentionDto(val spoken: String? = null, val chain: String? = null, val place: String? = null)

@Serializable
internal data class DayRequestDto(val date: String? = null, val from: String? = null, val to: String? = null)

@Serializable
internal data class TimeSpanDto(val from: String? = null, val to: String? = null)

internal fun CinemaRequestDto.toCinemaRequest(calendar: Set<LocalDate>): CinemaRequest = CinemaRequest(
    venues = venues.mapNotNull { venue ->
        val spoken = venue.spoken?.trim().orEmpty().ifEmpty { listOfNotNull(venue.chain, venue.place).joinToString(" ") }
        if (spoken.isBlank()) return@mapNotNull null
        VenueMention(
            spoken = spoken,
            chain = Chain.entries.firstOrNull { it.name.equals(venue.chain?.trim(), ignoreCase = true) },
            place = venue.place?.trim()?.takeIf { it.isNotEmpty() },
        )
    },
    days = days.mapNotNull { day ->
        val date = day.date?.let { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }?.takeIf { it in calendar }
            ?: return@mapNotNull null
        DayRequest(date, timeSpan(day.from, day.to))
    }.distinctBy { it.date }.sortedBy { it.date },
    time = time?.let { timeSpan(it.from, it.to) },
    onlyFormats = onlyFormats.cleanTags(),
    excludedFormats = excludedFormats.cleanTags(),
    films = films.map { it.trim().trim('"', '«', '»', '“', '”') }.filter { it.isNotEmpty() }.distinct(),
)

private fun timeSpan(from: String?, to: String?): TimeSpan? {
    val span = TimeSpan(from.cleanTime(), to.cleanTime())
    return span.takeIf { it.from != null || it.to != null }
}

// "17" and "17.30" become "17:00" and "17:30"; anything else is dropped.
private fun String?.cleanTime(): String? {
    val text = this?.trim()?.replace('.', ':') ?: return null
    val withMinutes = if (text.all { it.isDigit() } && text.length in 1..2) "$text:00" else text
    return withMinutes.takeIf { timePattern.matches(it) }
}

private fun List<String>.cleanTags(): Set<String> = map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()

internal fun systemPrompt(calendar: List<LocalDate>): String = buildString {
    appendLine("You turn a request for cinema showtimes, spoken or typed in Italian or English, into JSON.")
    appendLine("Answer with one JSON object and nothing else, in this shape:")
    appendLine("""{"venues":[{"spoken":"...","chain":"UCI","place":"Marcon"}],"days":[{"date":"YYYY-MM-DD","from":"HH:MM","to":"HH:MM"}],"time":{"from":"HH:MM","to":"HH:MM"},"onlyFormats":["2D"],"excludedFormats":[],"films":["..."]}""")
    appendLine()
    appendLine("Rules:")
    appendLine("- venues: one entry per cinema mentioned. \"spoken\" is exactly how the user said it. \"chain\" is one of ${Chain.entries.joinToString { it.name }} when the words name or resemble it, otherwise null. \"place\" is the town or venue name without the chain (e.g. \"Silea\", \"Fiume Veneto\"), null when only the chain is named.")
    appendLine("- days: only dates from the calendar below. \"questo giovedì\" / \"this Thursday\" is the first Thursday in the calendar, today included. \"oggi\" is today, \"domani\" tomorrow. \"weekend\" / \"fine settimana\" is Saturday and Sunday. A range such as \"tra giovedì e domenica\" includes every day in between. Give each day the time window the user said for it; use null for a bound that was not said.")
    appendLine("- times: \"dalle 17 alle 20\" is from 17:00 to 20:00; \"dopo le 20\" or \"dalle 20\" is from 20:00 with to null; \"entro le 22\", \"fino alle 22\" or \"prima delle 22\" is to 22:00; \"sera\" is 18:00 to 24:00; \"pomeriggio\" 14:00 to 18:00; \"mattina\" 09:00 to 13:00. Midnight is 24:00.")
    appendLine("- time: a window said without any day; otherwise null.")
    appendLine("- onlyFormats / excludedFormats: formats in capitals, e.g. 2D, 3D, IMAX, 4DX, SCREENX, DOLBY ATMOS, VO (original language). \"solo 2D\" gives onlyFormats [\"2D\"]; \"niente 3D\" gives excludedFormats [\"3D\"].")
    appendLine("- films: the film titles as said, without quotes. A name that is not a cinema, a place, a day, a time or a format is a film title, even a single common word (e.g. \"Hope\" in \"Hope a UCI Marcon giovedì sera\", \"Dune\", \"Wicked\").")
    appendLine("- Leave out anything the user did not say: empty lists and null.")
    appendLine()
    appendLine("Calendar:")
    calendar.forEachIndexed { index, date ->
        val note = if (index == 0) " (today)" else ""
        appendLine("${date} ${date.dayOfWeek.englishName()} / ${date.dayOfWeek.italianName()}$note")
    }
}

private fun DayOfWeek.englishName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

private fun DayOfWeek.italianName(): String = when (this) {
    DayOfWeek.MONDAY -> "lunedì"
    DayOfWeek.TUESDAY -> "martedì"
    DayOfWeek.WEDNESDAY -> "mercoledì"
    DayOfWeek.THURSDAY -> "giovedì"
    DayOfWeek.FRIDAY -> "venerdì"
    DayOfWeek.SATURDAY -> "sabato"
    DayOfWeek.SUNDAY -> "domenica"
}
