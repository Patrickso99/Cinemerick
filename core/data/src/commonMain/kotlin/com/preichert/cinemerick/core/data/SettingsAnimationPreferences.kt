package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.AnimationPreferences
import com.russhwolf.settings.Settings

private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"

class SettingsAnimationPreferences(private val settings: Settings) : AnimationPreferences {

    override fun animationsEnabled(): Boolean = settings.getBoolean(KEY_ANIMATIONS_ENABLED, true)

    override fun setAnimationsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_ANIMATIONS_ENABLED, enabled)
    }
}
