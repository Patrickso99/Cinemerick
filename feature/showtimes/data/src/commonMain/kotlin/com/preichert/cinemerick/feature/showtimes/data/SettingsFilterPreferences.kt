package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Cinema
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.russhwolf.settings.Settings

private const val KEY_HIDDEN_TAGS = "hidden_format_tags"
private const val KEY_SELECTED_CINEMAS = "selected_cinemas"
private const val SEPARATOR = "\n"

class SettingsFilterPreferences(private val settings: Settings) : FilterPreferences {

    override fun getHiddenTags(): Set<String> = read(KEY_HIDDEN_TAGS).toSet()

    override fun setHiddenTags(tags: Set<String>) {
        settings.putString(KEY_HIDDEN_TAGS, tags.joinToString(SEPARATOR))
    }

    override fun getSelectedCinemas(): Set<Cinema>? {
        if (!settings.hasKey(KEY_SELECTED_CINEMAS)) return null
        val saved = read(KEY_SELECTED_CINEMAS).mapNotNull { name -> Cinema.entries.firstOrNull { it.name == name } }
        return saved.toSet().ifEmpty { null }
    }

    override fun setSelectedCinemas(cinemas: Set<Cinema>) {
        settings.putString(KEY_SELECTED_CINEMAS, cinemas.joinToString(SEPARATOR) { it.name })
    }

    private fun read(key: String): List<String> =
        settings.getString(key, "").split(SEPARATOR).filter { it.isNotEmpty() }
}
