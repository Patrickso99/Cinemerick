package com.preichert.cinemerick.feature.showtimes.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import assertk.assertions.isNull
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.testing.FakeAssistantRepository
import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.CinemaRequest
import com.preichert.cinemerick.feature.showtimes.domain.DayRequest
import com.preichert.cinemerick.feature.showtimes.domain.TimeSpan
import com.preichert.cinemerick.feature.showtimes.domain.VenueMention
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class LlmCinemaRequestInterpreterTest {

    // Wednesday 11 November 2026.
    private val today = LocalDate(2026, 11, 11)
    private val thursday = LocalDate(2026, 11, 12)
    private val friday = LocalDate(2026, 11, 13)
    private val saturday = LocalDate(2026, 11, 14)
    private val sunday = LocalDate(2026, 11, 15)

    private suspend fun interpret(answer: String): Result<CinemaRequest, AssistantError> =
        LlmCinemaRequestInterpreter(FakeAssistantRepository(Result.Success(answer))).interpret("…", today)

    private suspend fun request(answer: String): CinemaRequest = (interpret(answer) as Result.Success).data

    private fun days(entries: String) = """{"days":[$entries]}"""

    // ---------- The two requests of the design ----------

    @Test
    fun secondExampleRequestIsReadWithItsPerDayWindows() = runTest {
        val request = request(
            """{"venues":[{"spoken":"The Space Silea","chain":"THE_SPACE","place":"Silea"},{"spoken":"Uci Marcon","chain":"UCI","place":"Marcon"},{"spoken":"Notorius Fiume","chain":"NOTORIOUS","place":"Fiume"}],
               "days":[{"weekday":"thursday","from":"17:00","to":"20:00"},{"weekday":"friday","until":"sunday","from":"20:00","to":"24:00"}],
               "time":null,"onlyFormats":[],"excludedFormats":[],"films":["Hope"]}"""
        )

        assertThat(request.venues.map { it.chain }).containsExactly(Chain.THE_SPACE, Chain.UCI, Chain.NOTORIOUS)
        assertThat(request.days).isEqualTo(
            listOf(
                DayRequest(thursday, TimeSpan("17:00", "20:00")),
                DayRequest(friday, TimeSpan("20:00", "24:00")),
                DayRequest(saturday, TimeSpan("20:00", "24:00")),
                DayRequest(sunday, TimeSpan("20:00", "24:00")),
            )
        )
        assertThat(request.films).containsExactly("Hope")
    }

    @Test
    fun firstExampleRequestKeepsOnlyTwoD() = runTest {
        val request = request(
            """{"venues":[{"spoken":"UCI Marcon","chain":"UCI","place":"Marcon"}],"days":[{"weekday":"thursday","until":"friday","from":"17:00","to":"20:00"}],"onlyFormats":["2D"]}"""
        )

        assertThat(request.days.map { it.date }).containsExactly(thursday, friday)
        assertThat(request.onlyFormats).isEqualTo(setOf("2D"))
    }

    // ---------- What a small model really answered ----------

    @Test
    fun theAnswerOfAppleIntelligenceThatFailedIsNowRead() = runTest {
        // From an iPhone 16e: fenced, the window as a sentence, three cinemas in one entry, a format nobody asked for.
        val request = request(
            """```json
            {
              "venues": [{"spoken": "the space soles notorious fiume", "chain": "THE_SPACE", "place": "Fiume Veneto"}],
              "days": [
                {"date": "2026-11-12", "from": "20:00", "to": "24:00"},
                {"date": "2026-11-13", "from": null, "to": null}
              ],
              "time": "dalle 20:00 alle 24:00",
              "onlyFormats": ["2D"],
              "excludedFormats": [],
              "films": ["Hope"]
            }
            ```"""
        )

        assertThat(request.venues.map { it.spoken }).containsExactly("the space soles", "notorious fiume")
        assertThat(request.days).isEqualTo(listOf(DayRequest(thursday, TimeSpan("20:00", "24:00")), DayRequest(friday)))
        assertThat(request.time).isEqualTo(TimeSpan("20:00", "24:00"))
        assertThat(request.films).containsExactly("Hope")
    }

    @Test
    fun aFieldThatCannotBeReadIsDroppedNotTheWholeAnswer() = runTest {
        val request = request("""{"venues": 12, "days": "xyz", "time": [1, 2], "onlyFormats": {"a": 1}, "films": ["Hope"]}""")

        assertThat(request.films).containsExactly("Hope")
        assertThat(request.venues).isEmpty()
        assertThat(request.days).isEmpty()
        assertThat(request.time).isNull()
    }

    @Test
    fun oneNameWhereAListWasAskedCountsAsAListOfOne() = runTest {
        val request = request("""{"films":"Hope","onlyFormats":"2d","excludedFormats":" imax ","venues":{"spoken":"UCI Marcon"},"days":{"weekday":"friday"}}""")

        assertThat(request.films).containsExactly("Hope")
        assertThat(request.onlyFormats).isEqualTo(setOf("2D"))
        assertThat(request.excludedFormats).isEqualTo(setOf("IMAX"))
        assertThat(request.venues.map { it.spoken }).containsExactly("UCI Marcon")
        assertThat(request.days.map { it.date }).containsExactly(friday)
    }

    @Test
    fun answerWrappedInACodeFenceAndASentenceIsStillRead() = runTest {
        assertThat(request("Ecco il JSON:\n```json\n{\"films\":[\"Dune\"]}\n```\nSpero vada bene.").films).containsExactly("Dune")
    }

    @Test
    fun answerWithoutJsonIsABadAnswer() = runTest {
        listOf("", "Non ho capito", "{", "}{", "[1,2]", "{\"films\": [").forEach { answer ->
            assertThat(interpret(answer), name = "answer=\"$answer\"").isEqualTo(Result.Failure(AssistantError.BAD_ANSWER))
        }
    }

    @Test
    fun assistantFailurePassesThrough() = runTest {
        val result = LlmCinemaRequestInterpreter(FakeAssistantRepository(Result.Failure(AssistantError.INVALID_KEY))).interpret("…", today)

        assertThat(result).isEqualTo(Result.Failure(AssistantError.INVALID_KEY))
    }

    @Test
    fun emptyObjectIsAnEmptyRequest() = runTest {
        assertThat(request("{}").isEmpty).isEqualTo(true)
    }

    // ---------- Days ----------

    @Test
    fun weekdayNamesInEnglishAndItalianWithOrWithoutAccentsAndShortened() = runTest {
        listOf("thursday", "Thursday", "THU", "giovedì", "giovedi", "Gio").forEach { name ->
            assertThat(request(days("""{"weekday":"$name"}""")).days.map { it.date }, name = "weekday=$name").containsExactly(thursday)
        }
    }

    @Test
    fun aWeekdayIsTheFirstOneFromTodayAndTodayCounts() = runTest {
        assertThat(request(days("""{"weekday":"wednesday"}""")).days.map { it.date }).containsExactly(today)
        assertThat(request(days("""{"weekday":"tuesday"}""")).days.map { it.date }).containsExactly(LocalDate(2026, 11, 17))
    }

    @Test
    fun todayTomorrowAndTheWeekend() = runTest {
        assertThat(request(days("""{"weekday":"today"}""")).days.map { it.date }).containsExactly(today)
        assertThat(request(days("""{"weekday":"domani"}""")).days.map { it.date }).containsExactly(thursday)
        assertThat(request(days("""{"weekday":"weekend"}""")).days.map { it.date }).containsExactly(saturday, sunday)
    }

    @Test
    fun theWeekendOnASundayIsOnlyToday() = runTest {
        val onSunday = LlmCinemaRequestInterpreter(FakeAssistantRepository(Result.Success(days("""{"weekday":"weekend"}"""))))
            .interpret("…", sunday)

        assertThat((onSunday as Result.Success).data.days.map { it.date }).containsExactly(sunday)
    }

    @Test
    fun aRunCanWrapIntoTheNextWeek() = runTest {
        val request = request(days("""{"weekday":"saturday","until":"tuesday"}"""))

        assertThat(request.days.map { it.date }).containsExactly(saturday, sunday, LocalDate(2026, 11, 16), LocalDate(2026, 11, 17))
    }

    @Test
    fun aRunEndingOnItsOwnFirstDayIsOneDay() = runTest {
        assertThat(request(days("""{"weekday":"friday","until":"friday"}""")).days.map { it.date }).containsExactly(friday)
    }

    @Test
    fun anUnknownEndOfTheRunKeepsTheFirstDayOnly() = runTest {
        assertThat(request(days("""{"weekday":"friday","until":"blursday"}""")).days.map { it.date }).containsExactly(friday)
    }

    @Test
    fun unknownWeekdaysAndEmptyEntriesAreDropped() = runTest {
        val request = request(days("""{"weekday":"blursday"},{"weekday":""},{},12,null,{"from":"20:00"}"""))

        assertThat(request.days).isEmpty()
    }

    @Test
    fun aDateIsUsedWhenItIsNotInThePastNorMoreThanAYearAway() = runTest {
        val cases = mapOf(
            "2026-11-11" to listOf(today),
            "2026-11-26" to listOf(LocalDate(2026, 11, 26)),
            "2027-01-01" to listOf(LocalDate(2027, 1, 1)),
            "2027-11-11" to listOf(LocalDate(2027, 11, 11)),
            "2027-11-12" to emptyList(),
            "2026-11-10" to emptyList(),
            "2026-02-30" to emptyList(),
            "14/11/2026" to emptyList(),
            "nonsense" to emptyList(),
        )
        cases.forEach { (date, expected) ->
            assertThat(request(days("""{"date":"$date"}""")).days.map { it.date }, name = "date=$date").isEqualTo(expected)
        }
    }

    @Test
    fun aDateWithoutAYearIsTheFirstSuchDayFromTodayOn() = runTest {
        val cases = mapOf(
            "14/11" to LocalDate(2026, 11, 14), "15/12" to LocalDate(2026, 12, 15), "5-12" to LocalDate(2026, 12, 5),
            "15.12" to LocalDate(2026, 12, 15), "11/11" to today, " 3/1 " to LocalDate(2027, 1, 3), "10/11" to LocalDate(2027, 11, 10),
        )
        cases.forEach { (date, expected) ->
            assertThat(request(days("""{"date":"$date"}""")).days.map { it.date }, name = "date=$date").containsExactly(expected)
        }
        // Not a day of that month, not a month, not a date.
        listOf("31/02", "32/01", "15/13", "0/5", "15", "dicembre").forEach { date ->
            assertThat(request(days("""{"date":"$date"}""")).days, name = "date=$date").isEmpty()
        }
    }

    @Test
    fun aRunOfDatesIsRead() = runTest {
        // "dal 15 al 18 dicembre dalle 18 alle 24", said in November: this December.
        val request = request(days("""{"date":"15/12","untilDate":"18/12","from":"18:00","to":"24:00"}"""))

        assertThat(request.days).isEqualTo(
            (15..18).map { DayRequest(LocalDate(2026, 12, it), TimeSpan("18:00", "24:00")) }
        )
    }

    @Test
    fun aRunOfDatesCanCrossTheNewYear() = runTest {
        val request = request(days("""{"date":"30/12","untilDate":"02/01"}"""))

        assertThat(request.days.map { it.date }).containsExactly(
            LocalDate(2026, 12, 30), LocalDate(2026, 12, 31), LocalDate(2027, 1, 1), LocalDate(2027, 1, 2),
        )
    }

    @Test
    fun aRunEndingBeforeItsStartOrNotReadableIsTheFirstDayOnly() = runTest {
        listOf("""{"date":"18/12","untilDate":"15/12"}""", """{"date":"18/12","untilDate":"soon"}""", """{"date":"18/12","untilDate":"18/12"}""").forEach { entry ->
            assertThat(request(days(entry)).days.map { it.date }, name = entry).containsExactly(LocalDate(2026, 12, 18))
        }
    }

    @Test
    fun aVeryLongRunIsCutAtTwoWeeks() = runTest {
        val request = request(days("""{"date":"01/12","untilDate":"31/12"}"""))

        assertThat(request.days.size).isEqualTo(14)
        assertThat(request.days.first().date).isEqualTo(LocalDate(2026, 12, 1))
        assertThat(request.days.last().date).isEqualTo(LocalDate(2026, 12, 14))
    }

    @Test
    fun aDateInThePastFallsBackToTheWeekdayWhenThereIsOne() = runTest {
        assertThat(request(days("""{"date":"2026-11-01","weekday":"friday"}""")).days.map { it.date }).containsExactly(friday)
    }

    @Test
    fun theSameDayTwiceKeepsTheFirstAndTheDaysComeSorted() = runTest {
        val request = request(days("""{"weekday":"saturday","from":"20:00"},{"weekday":"friday"},{"weekday":"saturday","from":"18:00"}"""))

        assertThat(request.days).isEqualTo(listOf(DayRequest(friday), DayRequest(saturday, TimeSpan(from = "20:00"))))
    }

    // ---------- Times ----------

    @Test
    fun timesAreNormalizedOrDropped() = runTest {
        val cases = mapOf(
            "\"17\"" to "17:00", "\"9\"" to "9:00", "\"17.30\"" to "17:30", "\"24:00\"" to "24:00", "\"07:05\"" to "7:05",
            "20" to "20:00", "24" to "24:00", "17.5" to null,
            "\"25:00\"" to null, "\"24:30\"" to null, "\"17:5\"" to null, "\"17:60\"" to null, "\"-1\"" to null,
            "\"sera\"" to null, "\"\"" to null, "null" to null, "[20]" to null,
        )
        cases.forEach { (said, expected) ->
            val request = request(days("""{"weekday":"friday","from":$said}"""))
            assertThat(request.days.single().time?.from, name = "from=$said").isEqualTo(expected)
        }
    }

    @Test
    fun aWindowWrittenOutIsRead() = runTest {
        assertThat(request("""{"time":"dalle 20 alle 24"}""").time).isEqualTo(TimeSpan("20:00", "24:00"))
        assertThat(request("""{"time":"dopo le 20"}""").time).isEqualTo(TimeSpan(from = "20:00"))
        assertThat(request("""{"time":"fino alle 22"}""").time).isEqualTo(TimeSpan(to = "22:00"))
        assertThat(request("""{"time":"before 22:30"}""").time).isEqualTo(TimeSpan(to = "22:30"))
        assertThat(request("""{"time":"sera"}""").time).isNull()
        assertThat(request("""{"time":""}""").time).isNull()
    }

    @Test
    fun aWindowWithNoValidBoundIsNoWindow() = runTest {
        assertThat(request("""{"time":{"from":"tardi","to":null}}""").time).isNull()
        assertThat(request("""{"time":{}}""").time).isNull()
        assertThat(request("""{"time":{"from":"20:00"}}""").time).isEqualTo(TimeSpan(from = "20:00"))
    }

    // ---------- Cinemas, films, formats ----------

    @Test
    fun unknownChainBecomesNoChainAndMissingSpokenIsRebuilt() = runTest {
        val venues = request("""{"venues":[{"chain":"CINECITTA","place":"Roma"},{"chain":"uci","place":"Villesse"},{"spoken":"  "},12,"UCI Marcon",null]}""").venues

        assertThat(venues).containsExactly(
            VenueMention("CINECITTA Roma", null, "Roma"),
            VenueMention("uci Villesse", Chain.UCI, "Villesse"),
            VenueMention("UCI Marcon"),
        )
    }

    @Test
    fun severalCinemasInOneEntryAreSplit() = runTest {
        val venues = request("""{"venues":[{"spoken":"uci marcon the space silea","chain":"UCI"}]}""").venues

        assertThat(venues.map { it.spoken }).containsExactly("uci marcon", "the space silea")
    }

    @Test
    fun filmTitlesLoseQuotesAndDuplicates() = runTest {
        assertThat(request("""{"films":["«Hope»","\"Hope\"","  ","Wicked: Parte Due"]}""").films).containsExactly("Hope", "Wicked: Parte Due")
    }

    @Test
    fun formatsAreUppercasedAndBlanksIgnored() = runTest {
        val request = request("""{"onlyFormats":["2d"," "],"excludedFormats":["imax"]}""")

        assertThat(request.onlyFormats).isEqualTo(setOf("2D"))
        assertThat(request.excludedFormats).isEqualTo(setOf("IMAX"))
    }

    // ---------- The prompt ----------

    @Test
    fun thePromptGivesTodayAndNoListOfDates() = runTest {
        val assistant = FakeAssistantRepository(Result.Success("{}"))
        LlmCinemaRequestInterpreter(assistant).interpret("…", today)

        val prompt = assistant.lastSystem.orEmpty()
        assertThat(prompt.contains("Today is Wednesday 2026-11-11")).isEqualTo(true)
        // The dates are counted by the app: a list of them is what small models got wrong.
        assertThat(prompt.contains("2026-11-12")).isEqualTo(false)
        Chain.entries.forEach { assertThat(prompt.contains(it.name), name = it.name).isEqualTo(true) }
    }

    @Test
    fun thePromptFitsTheContextOfTheSmallestModel() {
        // Gemini Nano takes under about 4000 tokens in all, and a token is about 3 characters.
        assertThat(systemPrompt(today).length).isLessThan(5_500)
    }

    @Test
    fun theExamplesInThePromptAreNotTheKindOfValueAModelCopies() {
        // A model copied "2D" from a format example and applied a filter nobody asked for.
        assertThat(systemPrompt(today).contains("\"onlyFormats\":[\"")).isEqualTo(false)
    }
}
