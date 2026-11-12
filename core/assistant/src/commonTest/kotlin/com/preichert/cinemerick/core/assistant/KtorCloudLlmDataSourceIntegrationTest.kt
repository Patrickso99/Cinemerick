package com.preichert.cinemerick.core.assistant

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.preichert.cinemerick.core.data.HttpClientFactory
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class KtorCloudLlmDataSourceIntegrationTest {

    private val anthropic = CloudCredentials(CloudProvider.ANTHROPIC, "sk-ant-key", "claude-opus-5-5")
    private val openAi = CloudCredentials(CloudProvider.OPENAI, "sk-key", "gpt-5-mini")
    private val gemini = CloudCredentials(CloudProvider.GEMINI, "AIzaKey", "gemini-2.5-flash")

    private val requests = mutableListOf<HttpRequestData>()

    private fun dataSource(status: HttpStatusCode, body: String): KtorCloudLlmDataSource {
        val engine = MockEngine { request ->
            requests += request
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        return KtorCloudLlmDataSource(HttpClientFactory.create(engine))
    }

    @Test
    fun anthropicTextBlocksAreJoinedAndThinkingIsSkipped() = runTest {
        val source = dataSource(
            HttpStatusCode.OK,
            """{"content":[{"type":"thinking","thinking":""},{"type":"text","text":"{\"films\":"},{"type":"text","text":"[\"Hope\"]}"}],"stop_reason":"end_turn"}""",
        )

        assertThat(source.complete(anthropic, "s", "u")).isEqualTo(Result.Success("""{"films":["Hope"]}"""))
        val request = requests.single()
        assertThat(request.headers["x-api-key"]).isEqualTo("sk-ant-key")
        assertThat(request.headers["anthropic-version"]).isEqualTo("2026-10-21")
        assertThat(request.headers["anthropic-beta"]).isEqualTo("server-side-fallback-2026-07-01")
    }

    @Test
    fun olderClaudeModelGetsNoFallbackHeader() = runTest {
        val source = dataSource(HttpStatusCode.OK, """{"content":[{"type":"text","text":"{}"}]}""")

        source.complete(anthropic.copy(model = "claude-haiku-4-5"), "s", "u")

        assertThat(requests.single().headers["anthropic-beta"]).isNull()
    }

    @Test
    fun anthropicRefusalIsABadAnswer() = runTest {
        val source = dataSource(HttpStatusCode.OK, """{"content":[],"stop_reason":"refusal"}""")

        assertThat(source.complete(anthropic, "s", "u")).isEqualTo(Result.Failure(AssistantError.BAD_ANSWER))
    }

    @Test
    fun accountWithoutCreditIsTheQuotaError() = runTest {
        val source = dataSource(
            HttpStatusCode.BadRequest,
            """{"type":"error","error":{"type":"invalid_request_error","message":"Your credit balance is too low to access the Anthropic API."}}""",
        )

        assertThat(source.complete(anthropic, "s", "u")).isEqualTo(Result.Failure(AssistantError.RATE_LIMITED))
    }

    @Test
    fun otherBadRequestIsNotTheQuotaError() = runTest {
        val source = dataSource(HttpStatusCode.BadRequest, """{"error":{"message":"model: field required"}}""")

        assertThat(source.complete(anthropic, "s", "u")).isEqualTo(Result.Failure(AssistantError.UNKNOWN))
    }

    @Test
    fun overloadedProviderIsNotRetried() = runTest {
        val source = dataSource(HttpStatusCode.ServiceUnavailable, """{"error":{"message":"The model is overloaded"}}""")

        assertThat(source.complete(gemini, "s", "u")).isEqualTo(Result.Failure(AssistantError.UNAVAILABLE))
        assertThat(requests.size).isEqualTo(1)
    }

    @Test
    fun rejectedKeyIsTheInvalidKeyError() = runTest {
        val source = dataSource(HttpStatusCode.Unauthorized, """{"error":{"message":"invalid x-api-key"}}""")

        assertThat(source.complete(anthropic, "s", "u")).isEqualTo(Result.Failure(AssistantError.INVALID_KEY))
    }

    @Test
    fun openAiChatAnswerIsReadWithTheBearerKey() = runTest {
        val source = dataSource(HttpStatusCode.OK, """{"choices":[{"message":{"role":"assistant","content":"{}"}}]}""")

        assertThat(source.complete(openAi, "s", "u")).isEqualTo(Result.Success("{}"))
        assertThat(requests.single().url.toString()).isEqualTo("https://api.openai.com/v1/chat/completions")
        assertThat(requests.single().headers[HttpHeaders.Authorization]).isEqualTo("Bearer sk-key")
    }

    @Test
    fun chatAnswerWithoutContentIsABadAnswer() = runTest {
        val source = dataSource(HttpStatusCode.OK, """{"choices":[]}""")

        assertThat(source.complete(openAi, "s", "u")).isEqualTo(Result.Failure(AssistantError.BAD_ANSWER))
    }

    @Test
    fun geminiAnswerIsReadFromTheCandidateParts() = runTest {
        val source = dataSource(HttpStatusCode.OK, """{"candidates":[{"content":{"parts":[{"text":"{\"films\":[]}"}]}}]}""")

        assertThat(source.complete(gemini, "s", "u")).isEqualTo(Result.Success("""{"films":[]}"""))
        assertThat(requests.single().url.encodedPath).isEqualTo("/v1beta/models/gemini-2.5-flash:generateContent")
        assertThat(requests.single().headers["x-goog-api-key"]).isEqualTo("AIzaKey")
        assertThat(requests.single().bodyText().contains("\"thinkingBudget\":0")).isEqualTo(true)
    }

    @Test
    fun geminiProKeepsItsThinking() = runTest {
        val source = dataSource(HttpStatusCode.OK, """{"candidates":[{"content":{"parts":[{"text":"{}"}]}}]}""")

        source.complete(gemini.copy(model = "gemini-2.5-pro"), "s", "u")

        assertThat(requests.single().bodyText().contains("thinkingBudget")).isEqualTo(false)
    }

    @Test
    fun geminiMalformedKeyCheckIsTheInvalidKeyError() = runTest {
        val source = dataSource(HttpStatusCode.BadRequest, """{"error":{"message":"API key not valid","status":"INVALID_ARGUMENT"}}""")

        assertThat(source.verify(gemini)).isEqualTo(Result.Failure(AssistantError.INVALID_KEY))
    }

    @Test
    fun openRouterWithoutCreditOnTheKeyCheckIsTheQuotaError() = runTest {
        val source = dataSource(HttpStatusCode.PaymentRequired, """{"error":{"message":"Insufficient credits"}}""")

        assertThat(source.verify(openAi.copy(provider = CloudProvider.OPENROUTER))).isEqualTo(Result.Failure(AssistantError.RATE_LIMITED))
        assertThat(requests.single().url.toString()).isEqualTo("https://openrouter.ai/api/v1/key")
    }
}

private fun HttpRequestData.bodyText(): String = (body as TextContent).text
