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
import com.preichert.cinemerick.feature.showtimes.domain.splitVenueMention
import com.preichert.cinemerick.feature.showtimes.domain.titleKey
import com.preichert.cinemerick.feature.showtimes.domain.tomorrow
import com.preichert.cinemerick.feature.showtimes.domain.upcomingWeekend
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val log = Logger.withTag("LlmCinemaRequestInterpreter")

// A date said by the user can be any day of the coming year; a run of dates is cut at two weeks, as a long one means
// many searches.
private const val YEARS_AHEAD_DAYS = 365
private const val MAX_RUN_DAYS = 14

// An end further than this from the start is a mistake ("18/12" to "15/12" read as next year), not a run.
private const val MAX_RUN_SPAN_DAYS = 62

/**
 * Asks the user's assistant to read the request as JSON, then keeps only what makes sense. Models, the small ones on the
 * device above all, answer in a shape that is almost right: a window written as a sentence, a number for a time, one name
 * where a list was asked. So the answer is read field by field, and a field that cannot be read is dropped, not the
 * whole answer. Days are asked as weekdays and turned into dates here: counting dates is what small models get wrong.
 */
class LlmCinemaRequestInterpreter(private val assistant: AssistantRepository) : CinemaRequestInterpreter {

    override suspend fun interpret(text: String, today: LocalDate): Result<CinemaRequest, AssistantError> =
        when (val answer = assistant.complete(systemPrompt(today), text)) {
            is Result.Failure -> answer
            is Result.Success -> parseAnswer(answer.data, today)?.let { Result.Success(it) } ?: Result.Failure(AssistantError.BAD_ANSWER)
        }
}

// Models wrap the object in a code fence or a sentence: only the outermost braces are read.
internal fun parseAnswer(answer: String, today: LocalDate): CinemaRequest? {
    val start = answer.indexOf('{')
    val end = answer.lastIndexOf('}')
    if (start < 0 || end <= start) return null
    val root = try {
        Json { isLenient = true }.parseToJsonElement(answer.substring(start, end + 1)) as? JsonObject
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        log.w(e) { "Unreadable request" }
        null
    } ?: return null

    return CinemaRequest(
        venues = root.items("venues").mapNotNull(::venueOf).flatMap(::splitVenueMention),
        days = root.items("days").flatMap { daysOf(it, today) }.distinctBy { it.date }.sortedBy { it.date },
        time = timeOf(root["time"]),
        onlyFormats = root.texts("onlyFormats").cleanTags(),
        excludedFormats = root.texts("excludedFormats").cleanTags(),
        films = root.texts("films").map { it.trim().trim('"', '«', '»', '“', '”') }.filter { it.isNotEmpty() }.distinct(),
    )
}

// ---------- Reading fields ----------

/** The entries of a list; a single object or text where a list was asked counts as a list of one. */
private fun JsonObject.items(key: String): List<JsonElement> = when (val element = this[key]) {
    is JsonArray -> element
    null, JsonNull -> emptyList()
    else -> listOf(element)
}

/** The texts of a list, or the one text where a list was asked. */
private fun JsonObject.texts(key: String): List<String> = items(key).mapNotNull { it.text() }

private fun JsonElement?.text(): String? =
    (this as? JsonPrimitive)?.takeIf { it !is JsonNull }?.content?.trim()?.takeIf { it.isNotEmpty() }

private fun JsonObject.text(key: String): String? = this[key].text()

private fun venueOf(element: JsonElement): VenueMention? {
    // A bare name is a cinema; a number or a boolean is not.
    if (element is JsonPrimitive) return element.takeIf { it.isString }?.text()?.let { VenueMention(spoken = it) }
    val venue = element as? JsonObject ?: return null
    val chain = venue.text("chain")
    val place = venue.text("place")
    val spoken = venue.text("spoken") ?: listOfNotNull(chain, place).joinToString(" ")
    if (spoken.isBlank()) return null
    return VenueMention(
        spoken = spoken,
        chain = Chain.entries.firstOrNull { it.name.equals(chain, ignoreCase = true) },
        place = place,
    )
}

private val timeToken = Regex("""\d{1,2}(?:[:.]\d{2})?""")
private val untilWords = listOf("fino", "entro", "prima", "until", "before", "by ")

/** A window as an object, or written out ("dalle 20 alle 24"): the first time is its start, the second its end. */
private fun timeOf(element: JsonElement?): TimeSpan? {
    val span = when (element) {
        is JsonObject -> TimeSpan(cleanTime(element["from"]), cleanTime(element["to"]))
        is JsonPrimitive -> {
            val said = element.text()?.lowercase() ?: return null
            val times = timeToken.findAll(said).map { cleanTime(JsonPrimitive(it.value)) }.toList()
            when {
                times.size >= 2 -> TimeSpan(times[0], times[1])
                times.size == 1 && untilWords.any { said.contains(it) } -> TimeSpan(to = times[0])
                times.size == 1 -> TimeSpan(from = times[0])
                else -> return null
            }
        }
        else -> return null
    }
    return span.takeIf { it.from != null || it.to != null }
}

private val timeText = Regex("""^(\d{1,2})(?:[:.](\d{2}))?$""")

/** "17" and "17.30" become "17:00" and "17:30"; a number works as text; anything that is not a time is dropped. */
private fun cleanTime(element: JsonElement?): String? {
    val match = timeText.matchEntire(element.text() ?: return null) ?: return null
    val hour = match.groupValues[1].toInt()
    val minutes = match.groupValues[2].ifEmpty { "00" }.toInt()
    // 24:00 is the end of the day, and only that.
    val valid = (hour in 0..23 && minutes in 0..59) || (hour == 24 && minutes == 0)
    return if (valid) "$hour:${minutes.toString().padStart(2, '0')}" else null
}

// ---------- Days ----------

private val weekdays: Map<String, DayOfWeek> = buildMap {
    val names = listOf(
        DayOfWeek.MONDAY to listOf("monday", "mon", "lunedi", "lun"),
        DayOfWeek.TUESDAY to listOf("tuesday", "tue", "martedi", "mar"),
        DayOfWeek.WEDNESDAY to listOf("wednesday", "wed", "mercoledi", "mer"),
        DayOfWeek.THURSDAY to listOf("thursday", "thu", "giovedi", "gio"),
        DayOfWeek.FRIDAY to listOf("friday", "fri", "venerdi", "ven"),
        DayOfWeek.SATURDAY to listOf("saturday", "sat", "sabato", "sab"),
        DayOfWeek.SUNDAY to listOf("sunday", "sun", "domenica", "dom"),
    )
    names.forEach { (day, spellings) -> spellings.forEach { put(it, day) } }
}

/** The dates one entry of "days" stands for, each with the window the entry gave. */
private fun daysOf(element: JsonElement, today: LocalDate): List<DayRequest> {
    val entry = when (element) {
        is JsonObject -> element
        is JsonPrimitive -> JsonObject(mapOf("weekday" to element))
        else -> return emptyList()
    }
    val time = TimeSpan(cleanTime(entry["from"]), cleanTime(entry["to"])).takeIf { it.from != null || it.to != null }
    return datesOf(entry.text("weekday"), entry.text("until"), entry.text("date"), entry.text("untilDate"), today).map { DayRequest(it, time) }
}

private fun datesOf(weekday: String?, until: String?, date: String?, untilDate: String?, today: LocalDate): List<LocalDate> {
    // Dates said as such ("15 dicembre", "dal 15 al 18") are used when they are not in the past and not too far away.
    val first = date?.let { dateOf(it, today) }?.takeIf { it >= today && it <= today.plus(YEARS_AHEAD_DAYS, DateTimeUnit.DAY) }
    if (first != null) {
        // The end of a run is the first such date from the start on: "28/12" to "03/01" crosses the new year.
        val last = untilDate?.let { dateOf(it, first) }?.takeIf { it >= first && it <= first.plus(MAX_RUN_SPAN_DAYS, DateTimeUnit.DAY) }
            ?: return listOf(first)
        return run(first, last)
    }

    val name = titleKey(weekday ?: return emptyList())
    when (name) {
        "today", "oggi" -> return listOf(today)
        "tomorrow", "domani" -> return listOf(tomorrow(today))
        "weekend", "fine settimana" -> return upcomingWeekend(today)
    }
    val firstDay = nextOccurrence(weekdays[name] ?: return emptyList(), today)
    val lastDay = until?.let { weekdays[titleKey(it)] }?.let { nextOccurrence(it, firstDay) } ?: return listOf(firstDay)
    return run(firstDay, lastDay)
}

private fun run(first: LocalDate, last: LocalDate): List<LocalDate> =
    generateSequence(first) { it.plus(1, DateTimeUnit.DAY) }.takeWhile { it <= last }.take(MAX_RUN_DAYS).toList()

private val dayMonth = Regex("""^(\d{1,2})[/.-](\d{1,2})$""")

/**
 * A date as the model writes it: "2026-12-15", or "15/12" as Italians write it (day first). Without a year it is the
 * first such day from [from] on, so "15/12" said in November is this December and "03/01" said in December is January.
 */
private fun dateOf(text: String, from: LocalDate): LocalDate? {
    runCatching { LocalDate.parse(text) }.getOrNull()?.let { return it }
    val match = dayMonth.matchEntire(text.trim()) ?: return null
    val day = match.groupValues[1].toInt()
    val month = match.groupValues[2].toInt()
    val thisYear = runCatching { LocalDate(from.year, month, day) }.getOrNull() ?: return null
    return if (thisYear >= from) thisYear else runCatching { LocalDate(from.year + 1, month, day) }.getOrNull()
}

// The first date from [from] on, [from] included, that falls on [day]: "this Thursday" said on a Thursday is today.
private fun nextOccurrence(day: DayOfWeek, from: LocalDate): LocalDate =
    from.plus((day.ordinal - from.dayOfWeek.ordinal + 7) % 7, DateTimeUnit.DAY)

private fun List<String>.cleanTags(): Set<String> = map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()

// ---------- The prompt ----------

internal fun systemPrompt(today: LocalDate): String = """
You read a request for cinema showtimes, spoken or typed in Italian or English, and answer with one JSON object and nothing else. The words come from speech recognition, so names are often misspelled: keep them as they are.

Today is ${today.dayOfWeek.englishName()} $today.

Fields. Leave out, or give an empty list or null for, everything the user did not say. Never make anything up.
- "venues": one entry for each cinema named: {"spoken": the words as said, "chain": ${Chain.entries.joinToString(" | ") { it.name }} or null, "place": the town or venue name without the chain, or null}. Every chain name starts a new entry.
- "days": one entry for each day or run of days: {"weekday": "monday" to "sunday", or "today", "tomorrow", "weekend"; "until": the last weekday of a run, like "friday" to "sunday"; "date": "DD/MM" when the user says a date, like "15 dicembre" is "15/12"; "untilDate": "DD/MM", the last date of a run: "dal 15 al 18 dicembre" is date "15/12" and untilDate "18/12"; "from": "HH:MM"; "to": "HH:MM"}. Use weekdays or dates, as the user said. Give each entry the times said for that day.
- "time": {"from": "HH:MM", "to": "HH:MM"} when times are said without any day, otherwise null.
- "onlyFormats" and "excludedFormats": lists in capitals, such as 2D, 3D, IMAX, 4DX, SCREENX, DOLBY ATMOS, VO (original language). "only 2D" is onlyFormats; "no 3D" is excludedFormats.
- "films": the film titles, without quotes. A name that is not a cinema, a place, a day, a time or a format is a film.

Times: "dalle 17 alle 20" is from 17:00 to 20:00; "dopo le 20" or "dalle 20" is from 20:00 only; "entro le 22", "fino alle 22" or "prima delle 22" is to 22:00 only; "sera" is 18:00 to 24:00; "pomeriggio" 14:00 to 18:00; "mattina" 09:00 to 13:00. Midnight is 24:00.

Three examples. They only show the format: never copy their values.
Request: "UCI Villesse e Cristallo Oderzo da venerdì a domenica dopo le 21"
{"venues":[{"spoken":"UCI Villesse","chain":"UCI","place":"Villesse"},{"spoken":"Cristallo Oderzo","chain":"CRISTALLO","place":"Oderzo"}],"days":[{"weekday":"friday","until":"sunday","from":"21:00","to":null}],"time":null,"onlyFormats":[],"excludedFormats":[],"films":[]}
Request: "Dune a Cinergia dal 3 al 5 gennaio dalle 20"
{"venues":[{"spoken":"Cinergia","chain":"CINERGIA","place":null}],"days":[{"date":"03/01","untilDate":"05/01","from":"20:00","to":null}],"time":null,"onlyFormats":[],"excludedFormats":[],"films":["Dune"]}
Request: "Anora al Cinemazero sabato sera, niente 3D"
{"venues":[{"spoken":"Cinemazero","chain":"CINEMAZERO","place":null}],"days":[{"weekday":"saturday","until":null,"from":"18:00","to":"24:00"}],"time":null,"onlyFormats":[],"excludedFormats":["3D"],"films":["Anora"]}
""".trimIndent()

private fun DayOfWeek.englishName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
