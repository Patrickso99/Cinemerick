package com.preichert.cinemerick.core.data

import com.preichert.cinemerick.core.domain.ThemeMode
import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsThemePreferencesTest {

    @Test
    fun followsSystemByDefault() {
        val preferences = SettingsThemePreferences(MemorySettings())

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode())
    }

    @Test
    fun remembersLightAndDarkAndSystem() {
        val preferences = SettingsThemePreferences(MemorySettings())

        preferences.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, preferences.themeMode())

        preferences.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, preferences.themeMode())

        preferences.setThemeMode(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, preferences.themeMode())
    }
}

class MemorySettings : Settings {
    private val values = mutableMapOf<String, Any>()

    override val keys: Set<String> get() = values.keys
    override val size: Int get() = values.size
    override fun clear() = values.clear()
    override fun remove(key: String) { values.remove(key) }
    override fun hasKey(key: String) = key in values

    override fun putInt(key: String, value: Int) { values[key] = value }
    override fun getInt(key: String, defaultValue: Int) = values[key] as? Int ?: defaultValue
    override fun getIntOrNull(key: String) = values[key] as? Int

    override fun putLong(key: String, value: Long) { values[key] = value }
    override fun getLong(key: String, defaultValue: Long) = values[key] as? Long ?: defaultValue
    override fun getLongOrNull(key: String) = values[key] as? Long

    override fun putString(key: String, value: String) { values[key] = value }
    override fun getString(key: String, defaultValue: String) = values[key] as? String ?: defaultValue
    override fun getStringOrNull(key: String) = values[key] as? String

    override fun putFloat(key: String, value: Float) { values[key] = value }
    override fun getFloat(key: String, defaultValue: Float) = values[key] as? Float ?: defaultValue
    override fun getFloatOrNull(key: String) = values[key] as? Float

    override fun putDouble(key: String, value: Double) { values[key] = value }
    override fun getDouble(key: String, defaultValue: Double) = values[key] as? Double ?: defaultValue
    override fun getDoubleOrNull(key: String) = values[key] as? Double

    override fun putBoolean(key: String, value: Boolean) { values[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean) = values[key] as? Boolean ?: defaultValue
    override fun getBooleanOrNull(key: String) = values[key] as? Boolean
}