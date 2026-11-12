package com.preichert.cinemerick.core.assistant

import com.preichert.cinemerick.core.domain.assistant.AssistantEngine
import com.preichert.cinemerick.core.domain.assistant.AssistantPreferences
import com.preichert.cinemerick.core.domain.assistant.CloudProvider
import com.russhwolf.settings.Settings

private const val KEY_ENGINE = "assistant_engine"
private const val KEY_PROVIDER = "assistant_provider"
private const val KEY_MODEL_PREFIX = "assistant_model_"

class SettingsAssistantPreferences(private val settings: Settings) : AssistantPreferences {

    override fun engine(): AssistantEngine? =
        settings.getStringOrNull(KEY_ENGINE)?.let { name -> AssistantEngine.entries.firstOrNull { it.name == name } }

    override fun setEngine(engine: AssistantEngine?) {
        if (engine == null) settings.remove(KEY_ENGINE) else settings.putString(KEY_ENGINE, engine.name)
    }

    override fun provider(): CloudProvider? =
        settings.getStringOrNull(KEY_PROVIDER)?.let { name -> CloudProvider.entries.firstOrNull { it.name == name } }

    override fun setProvider(provider: CloudProvider?) {
        if (provider == null) settings.remove(KEY_PROVIDER) else settings.putString(KEY_PROVIDER, provider.name)
    }

    override fun model(provider: CloudProvider): String? = settings.getStringOrNull(KEY_MODEL_PREFIX + provider.name)

    override fun setModel(provider: CloudProvider, model: String) {
        settings.putString(KEY_MODEL_PREFIX + provider.name, model)
    }
}
