package com.preichert.cinemerick.core.domain.assistant

import com.preichert.cinemerick.core.domain.Error

/** What answers the assistant's requests. Only [CLOUD] exists for now; the on-device engines follow. */
enum class AssistantEngine {
    CLOUD,
}

/**
 * The cloud providers the user can connect with their own API key. [defaultModel] is the model asked when the user
 * did not pick one; [keyPrefix] is how the provider's keys start, shown as a hint (empty when they have no prefix).
 */
enum class CloudProvider(
    val displayName: String,
    val defaultModel: String,
    val keyPrefix: String,
    val keyPageUrl: String,
) {
    OPENAI("OpenAI", "gpt-5-mini", "sk-", "https://platform.openai.com/api-keys"),
    ANTHROPIC("Anthropic", "claude-opus-5-5", "sk-ant-", "https://console.anthropic.com/settings/keys"),
    GEMINI("Google Gemini", "gemini-2.5-flash", "AIza", "https://aistudio.google.com/apikey"),
    GROK("xAI Grok", "grok-3-mini", "xai-", "https://console.x.ai"),
    MISTRAL("Mistral", "mistral-small-latest", "", "https://console.mistral.ai/api-keys"),
    OPENROUTER("OpenRouter", "openai/gpt-5-mini", "sk-or-", "https://openrouter.ai/keys"),
}

/** A provider, the user's key for it and the model to ask. */
data class CloudCredentials(
    val provider: CloudProvider,
    val apiKey: String,
    val model: String,
)

/** Why the assistant could not answer. */
enum class AssistantError : Error {
    /** No engine is set up yet: no provider with a key. */
    NO_ENGINE,
    /** The provider refused the key (revoked, expired, mistyped). */
    INVALID_KEY,
    /** The provider's rate limit or the account's credit was reached. */
    RATE_LIMITED,
    /** No connection. */
    OFFLINE,
    /** The provider did not answer in time or is down. */
    UNAVAILABLE,
    /** The answer could not be read as a request. */
    BAD_ANSWER,
    UNKNOWN,
}

/** Whether the assistant can answer, and with what: shown as the engine badge in the voice sheet. */
sealed interface AssistantStatus {
    data object NeedsSetup : AssistantStatus
    data class Ready(val engine: AssistantEngine, val provider: CloudProvider) : AssistantStatus
}

/** The key saved for a provider, masked for display: only its last four characters are kept. */
data class SavedKey(val provider: CloudProvider, val lastFour: String, val model: String)
