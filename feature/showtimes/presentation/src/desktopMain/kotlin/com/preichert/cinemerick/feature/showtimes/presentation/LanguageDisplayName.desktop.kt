package com.preichert.cinemerick.feature.showtimes.presentation

import java.util.Locale

internal actual fun languageDisplayName(code: String): String? {
    val tag = code.trim().lowercase()
    if (tag.isEmpty()) return null
    val appLocale = Locale.getDefault()
    // Java answers with the code itself for a language it does not know.
    return Locale.forLanguageTag(tag).getDisplayLanguage(appLocale)
        .takeIf { it.isNotBlank() && !it.equals(tag, ignoreCase = true) }
        ?.replaceFirstChar { it.titlecase(appLocale) }
}
