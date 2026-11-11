package com.preichert.cinemerick.feature.showtimes.domain

import kotlinx.datetime.LocalDate

/**
 * A person of the film world, from TMDB: their profile and the films they worked on, most recent first. Only the
 * fields TMDB has are set; [photoUrl] and [posterUrl] are absolute.
 */
data class PersonDetails(
    val id: Int,
    val name: String,
    val biography: String? = null,
    val birthday: LocalDate? = null,
    val deathday: LocalDate? = null,
    val placeOfBirth: String? = null,
    val photoUrl: String? = null,
    val films: List<PersonFilm> = emptyList()
)

/** A film a person worked on: [role] is the character played (TMDB's job titles are in English, so they are left out), [year] the release year. */
data class PersonFilm(
    val id: Int,
    val title: String,
    val year: Int? = null,
    val role: String? = null,
    val posterUrl: String? = null,
    val score: Double? = null
)
