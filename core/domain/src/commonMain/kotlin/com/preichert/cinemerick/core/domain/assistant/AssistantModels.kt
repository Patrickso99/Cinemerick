package com.preichert.cinemerick.core.domain.assistant

import com.preichert.cinemerick.core.domain.Error

/**
 * What answers the assistant's requests: the AI the system itself offers ([SYSTEM]: Gemini Nano on Android, Apple
 * Intelligence on iOS, nothing leaves the device) or a cloud provider with the user's own key ([CLOUD]).
 */
enum class AssistantEngine {
    SYSTEM,
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

/**
 * Whether the assistant can answer, and with what: shown as the engine badge in the voice sheet. A [CLOUD] engine
 * always has its [provider]; the [SYSTEM] one has none.
 */
sealed interface AssistantStatus {
    data object NeedsSetup : AssistantStatus
    data class Ready(val engine: AssistantEngine, val provider: CloudProvider? = null) : AssistantStatus
}

/** Whether the AI of the system can be used on this device, and what is missing when it cannot yet. */
enum class SystemAiAvailability {
    /** This device or platform has no such AI (an older phone, a locked-out bootloader, desktop). */
    UNSUPPORTED,
    /** The device could run it, but the user has to turn it on in the system settings (Apple Intelligence). */
    NEEDS_ENABLING,
    /** The system is still preparing it by itself: it becomes available without the user doing anything. */
    NOT_READY,
    /** It can be downloaded now (Gemini Nano, handled by Google's AICore). */
    DOWNLOADABLE,
    DOWNLOADING,
    AVAILABLE,
}

/**
 * The system AI as the screens show it. [name] is how it is called ("Gemini Nano", "Apple Intelligence"), null on a
 * platform that has none at all, where no row for it is shown. [downloadedBytes] grows while it is downloading.
 */
data class SystemAiState(
    val name: String? = null,
    val availability: SystemAiAvailability = SystemAiAvailability.UNSUPPORTED,
    val downloadedBytes: Long? = null,
)

/** Progress of the download of the system AI. */
sealed interface SystemAiDownload {
    data class Progress(val bytes: Long) : SystemAiDownload
    data object Completed : SystemAiDownload
    data object Failed : SystemAiDownload
}

/** The key saved for a provider, masked for display: only its last four characters are kept. */
data class SavedKey(val provider: CloudProvider, val lastFour: String, val model: String)
