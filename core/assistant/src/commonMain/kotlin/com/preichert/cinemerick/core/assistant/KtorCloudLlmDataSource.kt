package com.preichert.cinemerick.core.assistant

import co.touchlab.kermit.Logger
import com.preichert.cinemerick.core.data.safeResponse
import com.preichert.cinemerick.core.domain.DataError
import com.preichert.cinemerick.core.domain.EmptyResult
import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.core.domain.asEmptyResult
import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.CloudCredentials
import com.preichert.cinemerick.core.domain.assistant.CloudLlmRemoteDataSource
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.retry
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

private val log = Logger.withTag("KtorCloudLlmDataSource")

private const val ANTHROPIC_VERSION = "2026-10-21"
private const val ANTHROPIC_FALLBACK_BETA = "server-side-fallback-2026-07-01"
private const val ANTHROPIC_MAX_TOKENS = 4000
private const val REQUEST_TIMEOUT_MILLIS = 20_000L

// Claude models that take output_config.effort; on older ones the field is an error.
private val anthropicEffortModels = listOf("claude-opus-5", "claude-sonnet-5", "claude-haiku-5", "claude-fable-5", "claude-opus-4-6", "claude-opus-4-7", "claude-opus-4-8")
// Claude models that can be refused by a safety classifier and accept server-side fallbacks.
private val anthropicFallbackModels = setOf("claude-opus-5-5", "claude-sonnet-5-5", "claude-fable-5-2", "claude-fable-5-1", "claude-opus-5")

/**
 * One request to a provider's API with the user's own key. Four providers speak the OpenAI chat format, Anthropic and
 * Gemini have their own. Every answer is asked as a JSON object.
 */
class KtorCloudLlmDataSource(private val httpClient: HttpClient) : CloudLlmRemoteDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    override suspend fun complete(credentials: CloudCredentials, system: String, user: String): Result<String, AssistantError> =
        when (credentials.provider) {
            CloudProvider.ANTHROPIC -> anthropic(credentials, system, user)
            CloudProvider.GEMINI -> gemini(credentials, system, user)
            else -> chat(credentials, system, user)
        }

    // The model list answers only to a valid key; OpenRouter's is public, so its key endpoint is asked instead.
    override suspend fun verify(credentials: CloudCredentials): EmptyResult<AssistantError> {
        val url = when (credentials.provider) {
            CloudProvider.ANTHROPIC -> "https://api.anthropic.com/v1/models"
            CloudProvider.GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models"
            CloudProvider.OPENROUTER -> "https://openrouter.ai/api/v1/key"
            else -> "${chatBaseUrl(credentials.provider)}/models"
        }
        var refusal: String? = null
        val result = safeResponse {
            httpClient.get(url) {
                authorize(credentials)
                timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
                retry { noRetry() }
            }.also { refusal = readRefusal(credentials.provider, it) }
        }
        return when (result) {
            is Result.Success -> result.asEmptyResult()
            is Result.Failure -> Result.Failure(
                when {
                    refusal.isOutOfCredit() -> AssistantError.RATE_LIMITED
                    // Gemini answers a malformed key with 400 instead of 401.
                    result.error == DataError.Network.BAD_REQUEST -> AssistantError.INVALID_KEY
                    else -> result.error.toAssistantError()
                }
            )
        }
    }

    private suspend fun chat(credentials: CloudCredentials, system: String, user: String): Result<String, AssistantError> {
        val body = ChatRequestDto(
            model = credentials.model,
            messages = listOf(ChatMessageDto("system", system), ChatMessageDto("user", user)),
        )
        return post("${chatBaseUrl(credentials.provider)}/chat/completions", credentials, json.encodeToString(body)) { text ->
            json.decodeFromString<ChatResponseDto>(text).choices.firstOrNull()?.message?.content
        }
    }

    private suspend fun anthropic(credentials: CloudCredentials, system: String, user: String): Result<String, AssistantError> {
        val model = credentials.model
        val body = AnthropicRequestDto(
            model = model,
            maxTokens = ANTHROPIC_MAX_TOKENS,
            system = system,
            messages = listOf(ChatMessageDto("user", user)),
            // A short extraction: the lowest effort answers fastest.
            outputConfig = if (anthropicEffortModels.any { model.startsWith(it) }) AnthropicOutputConfigDto("low") else null,
            fallbacks = if (model in anthropicFallbackModels) "default" else null,
        )
        return post("https://api.anthropic.com/v1/messages", credentials, json.encodeToString(body)) { text ->
            val response = json.decodeFromString<AnthropicResponseDto>(text)
            if (response.stopReason == "refusal") null
            else response.content.filter { it.type == "text" }.mapNotNull { it.text }.joinToString("").ifBlank { null }
        }
    }

    private suspend fun gemini(credentials: CloudCredentials, system: String, user: String): Result<String, AssistantError> {
        val body = GeminiRequestDto(
            systemInstruction = GeminiContentDto(parts = listOf(GeminiPartDto(system))),
            contents = listOf(GeminiContentDto(parts = listOf(GeminiPartDto(user)), role = "user")),
            // Flash thinks by default, which only slows down a short extraction; it is the one model that allows 0.
            generationConfig = GeminiGenerationConfigDto(
                thinkingConfig = if (credentials.model.startsWith("gemini-2.5-flash")) GeminiThinkingConfigDto(0) else null,
            ),
        )
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${credentials.model}:generateContent"
        return post(url, credentials, json.encodeToString(body)) { text ->
            json.decodeFromString<GeminiResponseDto>(text).candidates.firstOrNull()?.content?.parts
                ?.mapNotNull { it.text }?.joinToString("")?.ifBlank { null }
        }
    }

    // Sends [body] and reads the answer text with [read]; an answer without text is a bad answer.
    private suspend fun post(
        url: String,
        credentials: CloudCredentials,
        body: String,
        read: (String) -> String?,
    ): Result<String, AssistantError> {
        var refusal: String? = null
        val result = safeResponse {
            httpClient.post(url) {
                authorize(credentials)
                // The user is waiting on the sheet: a slow or overloaded provider fails fast, and Try again is theirs to tap.
                timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
                retry { noRetry() }
                setBody(TextContent(body, ContentType.Application.Json))
            }.also { response -> refusal = readRefusal(credentials.provider, response) }
        }
        val response = when (result) {
            is Result.Failure -> return Result.Failure(
                if (refusal.isOutOfCredit()) AssistantError.RATE_LIMITED else result.error.toAssistantError()
            )
            is Result.Success -> result.data
        }
        val text = try {
            read(response.bodyAsText())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.w(e) { "Unreadable answer from ${credentials.provider}" }
            null
        }
        return if (text != null) Result.Success(text) else Result.Failure(AssistantError.BAD_ANSWER)
    }

    // The provider says why it refused in the body (never the key itself): logged, and read to tell an account without
    // credit (a 400 at Anthropic, a 402 at OpenRouter) from a bad request.
    private suspend fun readRefusal(provider: CloudProvider, response: HttpResponse): String? {
        val millis = response.responseTime.timestamp - response.requestTime.timestamp
        log.i { "$provider answered ${response.status.value} in ${millis}ms" }
        if (response.status.value < 400) return null
        val text = response.bodyAsText().take(500)
        log.w { "$provider answered ${response.status.value}: $text" }
        return text
    }

    private fun HttpRequestBuilder.authorize(credentials: CloudCredentials) {
        when (credentials.provider) {
            CloudProvider.ANTHROPIC -> {
                header("x-api-key", credentials.apiKey)
                header("anthropic-version", ANTHROPIC_VERSION)
                if (credentials.model in anthropicFallbackModels) header("anthropic-beta", ANTHROPIC_FALLBACK_BETA)
            }
            CloudProvider.GEMINI -> header("x-goog-api-key", credentials.apiKey)
            else -> header(HttpHeaders.Authorization, "Bearer ${credentials.apiKey}")
        }
    }

    private fun chatBaseUrl(provider: CloudProvider): String = when (provider) {
        CloudProvider.OPENAI -> "https://api.openai.com/v1"
        CloudProvider.GROK -> "https://api.x.ai/v1"
        CloudProvider.MISTRAL -> "https://api.mistral.ai/v1"
        CloudProvider.OPENROUTER -> "https://openrouter.ai/api/v1"
        CloudProvider.ANTHROPIC, CloudProvider.GEMINI -> error("$provider does not use the chat format")
    }
}

internal fun DataError.Network.toAssistantError(): AssistantError = when (this) {
    DataError.Network.UNAUTHORIZED, DataError.Network.FORBIDDEN -> AssistantError.INVALID_KEY
    DataError.Network.TOO_MANY_REQUESTS -> AssistantError.RATE_LIMITED
    DataError.Network.NO_INTERNET -> AssistantError.OFFLINE
    DataError.Network.REQUEST_TIMEOUT, DataError.Network.SERVER_ERROR, DataError.Network.SERVICE_UNAVAILABLE -> AssistantError.UNAVAILABLE
    DataError.Network.SERIALIZATION -> AssistantError.BAD_ANSWER
    else -> AssistantError.UNKNOWN
}

private val outOfCreditWords = listOf("credit", "billing", "quota", "insufficient_quota", "payment")

internal fun String?.isOutOfCredit(): Boolean = this != null && outOfCreditWords.any { contains(it, ignoreCase = true) }
