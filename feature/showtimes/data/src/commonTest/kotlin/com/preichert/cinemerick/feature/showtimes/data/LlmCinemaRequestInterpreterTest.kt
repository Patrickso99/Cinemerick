package com.preichert.cinemerick.feature.showtimes.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
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

    private suspend fun interpret(answer: String): Result<CinemaRequest, AssistantError> =
        LlmCinemaRequestInterpreter(FakeAssistantRepository(Result.Success(answer))).interpret("…", today)

    @Test
    fun secondExampleRequestIsReadWithItsPerDayWindows() = runTest {
        val result = interpret(
            """{"venues":[{"spoken":"The Space Silea","chain":"THE_SPACE","place":"Silea"},{"spoken":"Uci Marcon","chain":"UCI","place":"Marcon"},{"spoken":"Notorius Fiume","chain":"NOTORIOUS","place":"Fiume"}],
               "days":[{"date":"2026-11-12","from":"17:00","to":"20:00"},{"date":"2026-11-13","from":"20:00","to":"24:00"},{"date":"2026-11-14","from":"20:00","to":"24:00"},{"date":"2026-11-15","from":"20:00","to":"24:00"}],
               "time":null,"onlyFormats":[],"excludedFormats":[],"films":["Hope"]}"""
        )

        val request = (result as Result.Success).data
        assertThat(request.venues.map { it.chain }).containsExactly(Chain.THE_SPACE, Chain.UCI, Chain.NOTORIOUS)
        assertThat(request.days.first()).isEqualTo(DayRequest(LocalDate(2026, 11, 12), TimeSpan("17:00", "20:00")))
        assertThat(request.days.last()).isEqualTo(DayRequest(LocalDate(2026, 11, 15), TimeSpan("20:00", "24:00")))
        assertThat(request.films).containsExactly("Hope")
    }

    @Test
    fun answerWrappedInACodeFenceAndASentenceIsStillRead() = runTest {
        val result = interpret("Ecco il JSON:\n```json\n{\"films\":[\"Dune\"]}\n```")

        assertThat(result).isInstanceOf<Result.Success<CinemaRequest>>().prop(Result.Success<CinemaRequest>::data)
            .prop(CinemaRequest::films).containsExactly("Dune")
    }

    @Test
    fun answerWithoutJsonIsABadAnswer() = runTest {
        listOf("", "Non ho capito", "{", "}{", "{\"venues\": 12}").forEach { answer ->
            assertThat(interpret(answer), name = "answer=\"$answer\"").isEqualTo(Result.Failure(AssistantError.BAD_ANSWER))
        }
    }

    @Test
    fun assistantFailurePassesThrough() = runTest {
        val result = LlmCinemaRequestInterpreter(FakeAssistantRepository(Result.Failure(AssistantError.INVALID_KEY))).interpret("…", today)

        assertThat(result).isEqualTo(Result.Failure(AssistantError.INVALID_KEY))
    }

    @Test
    fun datesOutsideTheCalendarOrMalformedAreDropped() = runTest {
        val result = interpret(
            """{"days":[{"date":"2026-11-10"},{"date":"2026-11-26"},{"date":"12/11/2026"},{"date":"2026-02-30"},{"date":"2026-11-11"}]}"""
        )

        assertThat((result as Result.Success).data.days).containsExactly(DayRequest(today))
    }

    @Test
    fun sameDayTwiceKeepsTheFirst() = runTest {
        val result = interpret("""{"days":[{"date":"2026-11-13","from":"20:00"},{"date":"2026-11-12"},{"date":"2026-11-13","from":"18:00"}]}""")

        assertThat((result as Result.Success).data.days).containsExactly(
            DayRequest(LocalDate(2026, 11, 12)),
            DayRequest(LocalDate(2026, 11, 13), TimeSpan(from = "20:00")),
        )
    }

    @Test
    fun timesAreNormalizedOrDropped() = runTest {
        val cases = mapOf(
            "17" to "17:00",
            "9" to "9:00",
            "17.30" to "17:30",
            "24:00" to "24:00",
            "25:00" to null,
            "17:5" to null,
            "sera" to null,
            "" to null,
        )
        cases.forEach { (said, expected) ->
            val result = interpret("""{"time":{"from":"$said"}}""") as Result.Success
            assertThat(result.data.time?.from, name = "from=\"$said\"").isEqualTo(expected)
        }
    }

    @Test
    fun windowWithNoValidBoundIsNoWindow() = runTest {
        val result = interpret("""{"time":{"from":"tardi","to":null}}""") as Result.Success

        assertThat(result.data.time).isNull()
    }

    @Test
    fun unknownChainBecomesNoChainAndMissingSpokenIsRebuilt() = runTest {
        val result = interpret("""{"venues":[{"chain":"CINECITTA","place":"Roma"},{"chain":"uci","place":"Villesse"},{"spoken":"  "}]}""") as Result.Success

        assertThat(result.data.venues).containsExactly(
            VenueMention("CINECITTA Roma", null, "Roma"),
            VenueMention("uci Villesse", Chain.UCI, "Villesse"),
        )
    }

    @Test
    fun filmTitlesLoseQuotesAndDuplicates() = runTest {
        val result = interpret("""{"films":["«Hope»","\"Hope\"","  ","Wicked: Parte Due"]}""") as Result.Success

        assertThat(result.data.films).containsExactly("Hope", "Wicked: Parte Due")
    }

    @Test
    fun formatsAreUppercasedAndBlanksIgnored() = runTest {
        val result = interpret("""{"onlyFormats":["2d"," "],"excludedFormats":["imax"]}""") as Result.Success

        assertThat(result.data.onlyFormats).isEqualTo(setOf("2D"))
        assertThat(result.data.excludedFormats).isEqualTo(setOf("IMAX"))
    }

    @Test
    fun emptyObjectIsAnEmptyRequest() = runTest {
        val result = interpret("{}") as Result.Success

        assertThat(result.data.isEmpty).isEqualTo(true)
        assertThat(result.data.venues).isEmpty()
    }

    @Test
    fun calendarInThePromptStartsTodayAndNamesTheWeekday() = runTest {
        val assistant = FakeAssistantRepository(Result.Success("{}"))
        LlmCinemaRequestInterpreter(assistant).interpret("…", today)

        val prompt = assistant.lastSystem.orEmpty()
        assertThat(prompt.contains("2026-11-11 Wednesday / mercoledì (today)")).isEqualTo(true)
        assertThat(prompt.contains("2026-11-12 Thursday / giovedì")).isEqualTo(true)
        assertThat(prompt.contains("2026-11-25")).isEqualTo(true)
        assertThat(prompt.contains("2026-11-26")).isEqualTo(false)
    }
}
