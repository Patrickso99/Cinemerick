package com.preichert.cinemerick.feature.showtimes.presentation

import com.preichert.cinemerick.core.domain.Result
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import com.preichert.cinemerick.feature.showtimes.domain.FilmDetailsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Looks up the film database's details of the films whose cards come on screen. The answers are handed over in batches,
 * so a screenful of cards is one state update, not one each. A failed lookup is not remembered, so the card asks again
 * when it returns. Only used on the main thread, like the view model that owns it.
 */
internal class CardDetailsLoader(
    private val scope: CoroutineScope,
    private val filmDetailsRepository: FilmDetailsRepository,
    private val onAnswers: (Map<String, FilmDetails?>) -> Unit
) {
    // Titles being looked up right now, so a card that scrolls back in does not ask again.
    private val lookups = mutableSetOf<String>()
    // Answers since the last flush, and the flush that hands them over.
    private val answers = mutableMapOf<String, FilmDetails?>()
    private var flushJob: Job? = null

    // [resolved] is true when the answer for [title] is already in the state.
    fun load(title: String, resolved: Boolean) {
        if (resolved || title in answers || !lookups.add(title)) return
        scope.launch {
            try {
                val result = filmDetailsRepository.getDatabaseDetails(title)
                if (result is Result.Success) {
                    answers[title] = result.data
                    scheduleFlush()
                }
            } finally {
                lookups.remove(title)
            }
        }
    }

    private fun scheduleFlush() {
        if (flushJob?.isActive == true) return
        flushJob = scope.launch {
            delay(FLUSH_MILLIS)
            val batch = answers.toMap()
            answers.clear()
            onAnswers(batch)
        }
    }

    private companion object {
        const val FLUSH_MILLIS = 150L
    }
}
