package com.preichert.cinemerick.feature.showtimes.domain

private const val TAG_SEPARATOR = " · "
private const val DEFAULT_TAG = "2D"
private val DIMENSION_TAGS = setOf("2D", "3D")

private val Showing.rawTags: List<String>
    get() = format.orEmpty()
        .split(TAG_SEPARATOR)
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }

/**
 * Format tags of a showing, e.g. "2D · INFINITY VISION · VO" -> {2D, INFINITY VISION, VO}.
 * A showing that states no dimension (no format at all, or only e.g. "VO") is treated as 2D.
 */
val Showing.tags: Set<String>
    get() = rawTags.toSet().let { if (it.none { tag -> tag in DIMENSION_TAGS }) it + DEFAULT_TAG else it }

/** Format text for display: [tags] in their original order, with the implied 2D first. */
val Showing.displayFormat: String
    get() = (listOf(DEFAULT_TAG).filter { it in tags && it !in rawTags } + rawTags).joinToString(TAG_SEPARATOR)

fun List<Showing>.availableTags(): List<String> = flatMap { it.tags }.distinct().sorted()

/** Format tags the cinemas use, known before any search so a spoken "solo 2D" can hide the others right away. */
val COMMON_FORMAT_TAGS: Set<String> = setOf("2D", "3D", "IMAX", "4DX", "SCREENX", "DOLBY ATMOS", "INFINITY VISION", "EPIC", "VO")

// Tags about the language, not the screen: "solo 2D" keeps the original-language 2D showings.
private val LANGUAGE_TAGS = setOf("VO")

/**
 * The format tags to hide for "only these formats" and "not these formats". A showing is hidden when it has any hidden
 * tag, so "only" hides every other tag of the same kind ([knownTags] plus the common ones), and language tags are hidden
 * only when the user asked for a language.
 */
fun hiddenTagsFor(only: Set<String>, excluded: Set<String>, knownTags: Set<String>): Set<String> {
    val onlyTags = only.map { it.uppercase() }.toSet()
    val excludedTags = excluded.map { it.uppercase() }.toSet()
    val others = if (onlyTags.isEmpty()) {
        emptySet()
    } else {
        val universe = knownTags.map { it.uppercase() }.toSet() + COMMON_FORMAT_TAGS
        val keepLanguages = onlyTags.none { it in LANGUAGE_TAGS }
        val keepDimensions = onlyTags.none { it in DIMENSION_TAGS }
        universe.filterNot { tag ->
            tag in onlyTags || (keepLanguages && tag in LANGUAGE_TAGS) || (keepDimensions && tag in DIMENSION_TAGS)
        }.toSet()
    }
    return others + excludedTags
}
