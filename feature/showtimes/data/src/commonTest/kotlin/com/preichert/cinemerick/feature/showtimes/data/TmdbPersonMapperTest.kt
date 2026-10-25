package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.PersonDetails
import com.preichert.cinemerick.feature.showtimes.domain.PersonFilm
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TmdbPersonMapperTest {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Test
    fun mapsTheProfileAndTheFilmsMostRecentFirst() {
        val details = json.decodeFromString<TmdbPersonDto>(PERSON_JSON).toPersonDetails()

        assertEquals(
            PersonDetails(
                id = 42,
                name = "Rob Savage",
                biography = "Regista britannico.",
                birthday = LocalDate(1989, 5, 2),
                deathday = null,
                placeOfBirth = "Londra, Inghilterra",
                photoUrl = "https://image.tmdb.org/t/p/h632/rob.jpg",
                films = listOf(
                    PersonFilm(10, "Mammina", 2026, role = null, posterUrl = "https://image.tmdb.org/t/p/w342/mammina.jpg", score = 6.8),
                    PersonFilm(11, "Host", 2020, role = "Regia", posterUrl = null, score = null)
                )
            ),
            details
        )
    }

    @Test
    fun blankFieldsAreMissingAndSeriesAreLeftOut() {
        val details = json.decodeFromString<TmdbPersonDto>("""{"id":7,"name":"Anna","biography":"  ","birthday":"","combined_credits":{"cast":[{"id":3,"media_type":"tv","title":"Serie"}]}}""")
            .toPersonDetails()

        assertEquals(null, details.biography)
        assertEquals(null, details.birthday)
        assertEquals(emptyList(), details.films)
    }

    private companion object {
        const val PERSON_JSON = """
        {
          "id": 42,
          "name": "Rob Savage",
          "biography": "Regista britannico.",
          "birthday": "1989-05-02",
          "deathday": null,
          "place_of_birth": "Londra, Inghilterra",
          "known_for_department": "Directing",
          "profile_path": "/rob.jpg",
          "combined_credits": {
            "cast": [
              {"id": 10, "media_type": "movie", "title": "Mammina", "release_date": "2026-10-14", "character": "", "poster_path": "/mammina.jpg", "vote_average": 6.8},
              {"id": 99, "media_type": "tv", "title": "Una serie", "release_date": "2024-01-01", "character": "Ospite"}
            ],
            "crew": [
              {"id": 10, "media_type": "movie", "title": "Mammina", "release_date": "2026-10-14", "job": "Director"},
              {"id": 11, "media_type": "movie", "title": "Host", "release_date": "2020-07-15", "job": "Director", "character": "Regia"}
            ]
          }
        }
        """
    }
}
