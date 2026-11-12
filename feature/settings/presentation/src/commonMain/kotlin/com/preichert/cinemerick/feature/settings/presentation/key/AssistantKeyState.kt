package com.preichert.cinemerick.feature.settings.presentation.key

import com.preichert.cinemerick.core.domain.assistant.AssistantError
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.preichert.cinemerick.core.domain.assistant.SavedKey

data class AssistantKeyState(
    val provider: CloudProvider = CloudProvider.OPENAI,
    val key: String = "",
    val model: String = CloudProvider.OPENAI.defaultModel,
    val isKeyVisible: Boolean = false,
    val isVerifying: Boolean = false,
    /** Why the last check failed; cleared as soon as the key or the provider changes. */
    val error: KeyErrorUi? = null,
    /** Keys already saved on this device: the provider can be switched to without pasting its key again. */
    val savedKeys: Map<CloudProvider, SavedKey> = emptyMap(),
    val activeProvider: CloudProvider? = null,
) {
    val savedKey: SavedKey? get() = savedKeys[provider]
}

enum class KeyErrorUi { EMPTY, INVALID, OFFLINE, RATE_LIMITED, OTHER }

internal fun AssistantError.toKeyError(): KeyErrorUi = when (this) {
    AssistantError.INVALID_KEY -> KeyErrorUi.INVALID
    AssistantError.OFFLINE -> KeyErrorUi.OFFLINE
    AssistantError.RATE_LIMITED -> KeyErrorUi.RATE_LIMITED
    else -> KeyErrorUi.OTHER
}
