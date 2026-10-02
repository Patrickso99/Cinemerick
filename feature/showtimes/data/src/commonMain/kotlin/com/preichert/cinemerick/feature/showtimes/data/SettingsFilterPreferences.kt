package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.Chain
import com.preichert.cinemerick.feature.showtimes.domain.FilterPreferences
import com.preichert.cinemerick.feature.showtimes.domain.Venue
import com.russhwolf.settings.Settings

private const val KEY_SELECTED_VENUES = "selected_venues"
private const val KEY_SELECTED_CINEMAS = "selected_cinemas" // legacy
private const val RECORD_SEPARATOR = "\n"
private const val FIELD_SEPARATOR = "|"

class SettingsFilterPreferences(private val settings: Settings) : FilterPreferences {

    override fun getSelectedVenues(): Set<Venue>? {
        // Try new format first
        val saved = readVenues(KEY_SELECTED_VENUES)
        if (saved.isNotEmpty()) return saved.toSet().ifEmpty { null }

        // Migrate from old format
        if (settings.hasKey(KEY_SELECTED_CINEMAS)) {
            val oldCinemas = settings.getString(KEY_SELECTED_CINEMAS, "").split(RECORD_SEPARATOR).filter { it.isNotEmpty() }
            val migrated = oldCinemas.mapNotNull { name -> migrateOldCinema(name) }.toSet()
            if (migrated.isNotEmpty()) {
                setSelectedVenues(migrated)
                return migrated
            }
        }

        return null
    }

    override fun setSelectedVenues(venues: Set<Venue>) {
        val records = venues.map { venue ->
            listOf(venue.chain.name, venue.id, venue.name, venue.region.orEmpty(), venue.webUrl.orEmpty())
                .joinToString(FIELD_SEPARATOR)
        }
        settings.putString(KEY_SELECTED_VENUES, records.joinToString(RECORD_SEPARATOR))
        // Remove legacy key after migration so it doesn't resurface old selections
        if (settings.hasKey(KEY_SELECTED_CINEMAS)) {
            settings.remove(KEY_SELECTED_CINEMAS)
        }
    }

    private fun readVenues(key: String): List<Venue> {
        val records = settings.getString(key, "").split(RECORD_SEPARATOR).filter { it.isNotEmpty() }
        return records.mapNotNull { record ->
            val fields = record.split(FIELD_SEPARATOR)
            if (fields.size >= 3) {
                try {
                    Venue(
                        chain = Chain.valueOf(fields[0]),
                        id = fields[1],
                        name = fields[2],
                        region = fields.getOrNull(3)?.ifEmpty { null },
                        webUrl = fields.getOrNull(4)?.ifEmpty { null }
                    )
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    private fun migrateOldCinema(name: String): Venue? = when (name) {
        "THE_SPACE" -> Venue(Chain.THE_SPACE, "1009", "Silea", "Veneto")
        "UCI" -> Venue(Chain.UCI, "uci-cinemas-venezia-marcon", "UCI Luxe Marcon", "Veneto")
        "NOTORIOUS" -> Venue(Chain.NOTORIOUS, "ferrara", "Ferrara", "Emilia-Romagna")
        "CINERGIA" -> Venue(Chain.CINERGIA, "conegliano", "Conegliano", "Veneto")
        "CRISTALLO" -> Venue(Chain.CRISTALLO, "oderzo", "Oderzo", "Veneto")
        else -> null
    }
}
