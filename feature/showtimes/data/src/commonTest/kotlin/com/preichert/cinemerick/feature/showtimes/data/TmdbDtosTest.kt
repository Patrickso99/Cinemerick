package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private val tmdbJson = Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }

internal const val SEARCH_JSON = """
{
  "page": 1,
  "results": [
    {
      "adult": false,
      "backdrop_path": "/zqkmTXzjkAgXmEWLRsY4UpTWCeo.jpg",
      "genre_ids": [12, 28, 878],
      "id": 11,
      "original_language": "en",
      "original_title": "Star Wars",
      "overview": "Princess Leia is captured...",
      "popularity": 89.1924,
      "poster_path": "/fai0rspsNeJCS69wHNjOdWxcI7P.jpg",
      "release_date": "1977-05-25",
      "title": "Guerre stellari",
      "video": false,
      "vote_average": 8.207,
      "vote_count": 22999
    },
    {
      "id": 99,
      "original_title": "Senza data",
      "popularity": 1.5,
      "release_date": "",
      "title": "Senza data"
    },
    {
      "id": 100,
      "original_title": "Nessuna data",
      "popularity": null,
      "title": "Nessuna data"
    }
  ],
  "total_pages": 1,
  "total_results": 3
}
"""

internal const val MOVIE_JSON = """
{
  "adult": false,
  "genres": [{"id": 12, "name": "Avventura"}, {"id": 28, "name": "Azione"}],
  "id": 11,
  "imdb_id": "tt0076759",
  "original_title": "Star Wars",
  "overview": "La principessa Leia viene catturata.",
  "release_date": "1977-05-25",
  "runtime": 121,
  "title": "Guerre stellari",
  "vote_average": 8.207,
  "vote_count": 22999,
  "credits": {
    "cast": [
      {"name": "Mark Hamill", "character": "Luke Skywalker", "order": 0},
      {"name": "Harrison Ford", "character": "Han Solo", "order": 1}
    ],
    "crew": [
      {"name": "George Lucas", "job": "Director", "department": "Directing"},
      {"name": "Gary Kurtz", "job": "Producer", "department": "Production"}
    ]
  },
  "videos": {
    "results": [
      {"iso_639_1": "it", "iso_3166_1": "IT", "name": "Trailer", "key": "abc123", "site": "YouTube", "type": "Trailer", "official": true},
      {"iso_639_1": "en", "key": "def456", "site": "Vimeo", "type": "Teaser"}
    ]
  },
  "release_dates": {
    "results": [
      {"iso_3166_1": "US", "release_dates": [{"certification": "PG", "type": 3}]},
      {"iso_3166_1": "IT", "release_dates": [{"certification": "", "type": 3}, {"certification": "T", "type": 3}]}
    ]
  }
}
"""

class TmdbDtosTest {

    @Test
    fun decodesSearchResultsAndToleratesMissingFields() {
        val search = tmdbJson.decodeFromString<TmdbSearchDto>(SEARCH_JSON)

        assertEquals(3, search.results.size)
        val first = search.results.first()
        assertEquals(11, first.id)
        assertEquals("Guerre stellari", first.title)
        assertEquals("Star Wars", first.originalTitle)
        assertEquals("1977-05-25", first.releaseDate)
        assertEquals(89.1924, first.popularity)

        assertEquals("", search.results[1].releaseDate)
        assertNull(search.results[2].releaseDate)
        assertEquals(0.0, search.results[2].popularity)
    }

    @Test
    fun decodesMovieDetailWithAppendedCreditsVideosAndReleaseDates() {
        val movie = tmdbJson.decodeFromString<TmdbMovieDto>(MOVIE_JSON)

        assertEquals("Guerre stellari", movie.title)
        assertEquals("Star Wars", movie.originalTitle)
        assertEquals("La principessa Leia viene catturata.", movie.overview)
        assertEquals(121, movie.runtime)
        assertEquals(8.207, movie.voteAverage)
        assertEquals(22999, movie.voteCount)
        assertEquals(listOf("Avventura", "Azione"), movie.genres.map { it.name })

        val credits = assertNotNull(movie.credits)
        assertEquals(listOf("Mark Hamill", "Harrison Ford"), credits.cast.map { it.name })
        assertEquals(listOf("Director" to "George Lucas", "Producer" to "Gary Kurtz"), credits.crew.map { it.job to it.name })

        val videos = assertNotNull(movie.videos).results
        assertEquals(TmdbVideoDto(key = "abc123", site = "YouTube", type = "Trailer", official = true, language = "it"), videos.first())
        assertEquals("Vimeo", videos.last().site)

        val italy = assertNotNull(movie.releaseDates).results.first { it.country == "IT" }
        assertEquals(listOf("", "T"), italy.releaseDates.map { it.certification })
    }

    @Test
    fun movieWithoutAppendedSectionsDecodesWithNulls() {
        val movie = tmdbJson.decodeFromString<TmdbMovieDto>("""{"id": 5, "title": "Solo", "runtime": null, "overview": null}""")

        assertNull(movie.credits)
        assertNull(movie.videos)
        assertNull(movie.releaseDates)
        assertNull(movie.runtime)
        assertNull(movie.overview)
        assertEquals(0, movie.voteCount)
    }
}
