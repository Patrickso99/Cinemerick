package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.titleKey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private const val MONTHS_AHEAD = 6
private const val SUBTITLE_SEPARATOR = " - "

/**
 * The search result that is the film called [searchTitle], or null when none is trusted: a wrong film is worse than none.
 *
 * A candidate qualifies when its whole title or whole original title has the same [titleKey] as the searched title (so a
 * sequel or a "Title: Subtitle" film is not the film) and it is due at most [MONTHS_AHEAD] months after [today].
 * Cinemas often add an Italian subtitle after " - ": when nothing equals the full title, the part before it is tried,
 * and a full-title match always beats it. Among the candidates left the most popular wins (an obscure homonym must not
 * beat the real film), then the most recent. A classic re-released today qualifies like any other film.
 */
internal fun List<TmdbSearchResultDto>.bestMatchFor(searchTitle: String, today: LocalDate): TmdbSearchResultDto? {
    val full = titleKey(searchTitle)
    if (full.isEmpty()) return null
    val withoutSubtitle = titleKey(searchTitle.substringBefore(SUBTITLE_SEPARATOR)).takeIf { it.isNotEmpty() && it != full }
    val latestRelease = today.plus(MONTHS_AHEAD, DateTimeUnit.MONTH)

    val due = mapNotNull { candidate ->
        candidate.releaseDate?.toLocalDateOrNull()?.takeIf { it <= latestRelease }?.let { candidate to it }
    }

    fun matching(key: String) = due.filter { (candidate, _) -> titleKey(candidate.title) == key || titleKey(candidate.originalTitle) == key }

    val pool = matching(full).ifEmpty { withoutSubtitle?.let(::matching).orEmpty() }
    return pool.maxWithOrNull(compareBy({ it.first.popularity }, { it.second }))?.first
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
