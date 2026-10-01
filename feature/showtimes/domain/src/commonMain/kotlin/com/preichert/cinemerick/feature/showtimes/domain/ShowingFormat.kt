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
