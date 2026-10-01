package com.preichert.cinemerick.feature.showtimes.data

private val NUMERIC_ENTITY = Regex("&#(\\d+);")

private val NAMED_ENTITIES = mapOf(
    "&amp;" to "&", "&quot;" to "\"", "&apos;" to "'", "&nbsp;" to " ", "&lt;" to "<", "&gt;" to ">",
    "&agrave;" to "à", "&egrave;" to "è", "&eacute;" to "é", "&igrave;" to "ì", "&ograve;" to "ò", "&ugrave;" to "ù",
    "&Agrave;" to "À", "&Egrave;" to "È", "&Eacute;" to "É", "&Igrave;" to "Ì", "&Ograve;" to "Ò", "&Ugrave;" to "Ù"
)

internal fun String.unescapeHtml(): String =
    NAMED_ENTITIES.entries.fold(this) { text, (entity, char) -> text.replace(entity, char) }
        .replace(NUMERIC_ENTITY) { it.groupValues[1].toInt().toChar().toString() }
