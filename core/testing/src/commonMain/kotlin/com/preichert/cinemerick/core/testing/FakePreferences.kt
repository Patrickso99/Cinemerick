package com.preichert.cinemerick.core.testing

import com.preichert.cinemerick.core.domain.AnimationPreferences
import com.preichert.cinemerick.core.domain.AppLanguage
import com.preichert.cinemerick.core.domain.DEFAULT_MIN_MINUTES
import com.preichert.cinemerick.core.domain.LanguagePreferences
import com.preichert.cinemerick.core.domain.ThemeMode
import com.preichert.cinemerick.core.domain.ThemePreferences
import com.preichert.cinemerick.core.domain.TimeWindowPreferences

// In-memory preferences with the same defaults as the settings-backed ones in core:data.

class FakeThemePreferences(private var mode: ThemeMode = ThemeMode.SYSTEM) : ThemePreferences {
    override fun themeMode(): ThemeMode = mode
    override fun setThemeMode(mode: ThemeMode) {
        this.mode = mode
    }
}

class FakeLanguagePreferences(private var language: AppLanguage? = null) : LanguagePreferences {
    override fun language(): AppLanguage? = language
    override fun setLanguage(language: AppLanguage) {
        this.language = language
    }
}

class FakeAnimationPreferences(private var enabled: Boolean = true) : AnimationPreferences {
    override fun animationsEnabled(): Boolean = enabled
    override fun setAnimationsEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}

class FakeTimeWindowPreferences(
    private var min: Int = DEFAULT_MIN_MINUTES,
    private var max: Int? = null,
) : TimeWindowPreferences {
    override fun minMinutes(): Int = min
    override fun maxMinutes(): Int? = max
    override fun setMinMinutes(minutes: Int) {
        min = minutes
    }
    override fun setMaxMinutes(minutes: Int?) {
        max = minutes
    }
}
