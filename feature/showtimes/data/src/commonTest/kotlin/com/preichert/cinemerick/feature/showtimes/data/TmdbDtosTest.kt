package com.preichert.cinemerick.feature.showtimes.data

import kotlinx.serialization.json.Json
import kotlin.test.Test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull

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
  "tagline": "Tanto tempo fa",
  "backdrop_path": "/backdrop.jpg",
  "belongs_to_collection": {"id": 10, "name": "Saga di Guerre stellari"},
  "original_language": "en",
  "production_companies": [{"id": 1, "name": "Lucasfilm Ltd."}, {"id": 25, "name": "Twentieth Century Fox"}],
  "budget": 11000000,
  "revenue": 775398007,
  "images": {
    "backdrops": [
      {"file_path": "/backdrop.jpg", "vote_average": 7.0, "iso_639_1": null},
      {"file_path": "/b2.jpg", "vote_average": 6.1, "iso_639_1": "it"},
      {"file_path": "/b3.jpg", "vote_average": 3.0}
    ]
  },
  "production_countries": [{"iso_3166_1": "US", "name": "United States of America"}],
  "original_title": "Star Wars",
  "overview": "La principessa Leia viene catturata.",
  "release_date": "1977-05-25",
  "runtime": 121,
  "title": "Guerre stellari",
  "vote_average": 8.207,
  "vote_count": 22999,
  "credits": {
    "cast": [
      {"id": 2, "name": "Mark Hamill", "character": "Luke Skywalker", "profile_path": "/mark.jpg", "order": 0},
      {"id": 3, "name": "Harrison Ford", "character": "Han Solo", "order": 1}
    ],
    "crew": [
      {"id": 1, "name": "George Lucas", "job": "Director", "department": "Directing", "profile_path": "/george.jpg"},
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
      {"iso_3166_1": "IT", "release_dates": [{"certification": "", "type": 3, "release_date": "1978-01-01T00:00:00.000Z", "descriptors": []}, {"certification": "T", "type": 3, "release_date": "1977-10-20T00:00:00.000Z", "descriptors": []}]}
    ]
  }
}
"""

class TmdbDtosTest {

    @Test
    fun decodesSearchResultsAndToleratesMissingFields() {
        val search = tmdbJson.decodeFromString<TmdbSearchDto>(SEARCH_JSON)

        assertThat(search.results.size).isEqualTo(3)
        val first = search.results.first()
        assertThat(first.id).isEqualTo(11)
        assertThat(first.title).isEqualTo("Guerre stellari")
        assertThat(first.originalTitle).isEqualTo("Star Wars")
        assertThat(first.releaseDate).isEqualTo("1977-05-25")
        assertThat(first.popularity).isEqualTo(89.1924)

        assertThat(search.results[1].releaseDate).isEqualTo("")
        assertThat(search.results[2].releaseDate).isNull()
        assertThat(search.results[2].popularity).isEqualTo(0.0)
    }

    @Test
    fun decodesMovieDetailWithAppendedCreditsVideosAndReleaseDates() {
        val movie = tmdbJson.decodeFromString<TmdbMovieDto>(MOVIE_JSON)

        assertThat(movie.title).isEqualTo("Guerre stellari")
        assertThat(movie.originalTitle).isEqualTo("Star Wars")
        assertThat(movie.overview).isEqualTo("La principessa Leia viene catturata.")
        assertThat(movie.runtime).isEqualTo(121)
        assertThat(movie.voteAverage).isEqualTo(8.207)
        assertThat(movie.voteCount).isEqualTo(22999)
        assertThat(movie.genres.map { it.name }).isEqualTo(listOf("Avventura", "Azione"))
        assertThat(movie.tagline).isEqualTo("Tanto tempo fa")
        assertThat(movie.backdropPath).isEqualTo("/backdrop.jpg")
        assertThat(movie.collection?.name).isEqualTo("Saga di Guerre stellari")
        assertThat(movie.originalLanguage).isEqualTo("en")
        assertThat(movie.productionCountries.map { it.name }).isEqualTo(listOf("United States of America"))
        assertThat(movie.productionCompanies.map { it.name }).isEqualTo(listOf("Lucasfilm Ltd.", "Twentieth Century Fox"))
        assertThat(movie.budget).isEqualTo(11_000_000L)
        assertThat(movie.revenue).isEqualTo(775_398_007L)
        assertThat(movie.images).isNotNull()
        val images = movie.images!!
        assertThat(images.backdrops.map { it.filePath }).isEqualTo(listOf("/backdrop.jpg", "/b2.jpg", "/b3.jpg"))
        assertThat(images.backdrops.map { it.voteAverage }).isEqualTo(listOf(7.0, 6.1, 3.0))

        assertThat(movie.credits).isNotNull()
        val credits = movie.credits!!
        assertThat(credits.cast.map { it.name }).isEqualTo(listOf("Mark Hamill", "Harrison Ford"))
        assertThat(credits.crew.map { it.job to it.name }).isEqualTo(listOf("Director" to "George Lucas", "Producer" to "Gary Kurtz"))
        assertThat(credits.cast.map { it.id }).isEqualTo(listOf(2, 3))
        assertThat(credits.cast.map { it.character }).isEqualTo(listOf("Luke Skywalker", "Han Solo"))
        assertThat(credits.cast.map { it.profilePath }).isEqualTo(listOf("/mark.jpg", null))
        assertThat(credits.crew.first().profilePath).isEqualTo("/george.jpg")

        assertThat(movie.videos).isNotNull()
        val videos = movie.videos!!.results
        assertThat(videos.first()).isEqualTo(TmdbVideoDto(key = "abc123", site = "YouTube", type = "Trailer", official = true, language = "it"))
        assertThat(videos.last().site).isEqualTo("Vimeo")

        assertThat(movie.releaseDates).isNotNull()
        val italy = movie.releaseDates!!.results.first { it.country == "IT" }
        assertThat(italy.releaseDates.map { it.certification }).isEqualTo(listOf("", "T"))
        assertThat(italy.releaseDates.map { it.type }).isEqualTo(listOf(3, 3))
        assertThat(italy.releaseDates.last().releaseDate).isEqualTo("1977-10-20T00:00:00.000Z")
    }

    @Test
    fun movieWithoutAppendedSectionsDecodesWithNulls() {
        val movie = tmdbJson.decodeFromString<TmdbMovieDto>("""{"id": 5, "title": "Solo", "runtime": null, "overview": null}""")

        assertThat(movie.credits).isNull()
        assertThat(movie.videos).isNull()
        assertThat(movie.releaseDates).isNull()
        assertThat(movie.runtime).isNull()
        assertThat(movie.overview).isNull()
        assertThat(movie.tagline).isNull()
        assertThat(movie.backdropPath).isNull()
        assertThat(movie.collection).isNull()
        assertThat(movie.originalLanguage).isNull()
        assertThat(movie.images).isNull()
        assertThat(movie.budget).isEqualTo(0L)
        assertThat(movie.revenue).isEqualTo(0L)
        assertThat(movie.productionCompanies).isEmpty()
        assertThat(movie.productionCountries).isEmpty()
        assertThat(movie.voteCount).isEqualTo(0)
    }

    @Test
    fun descriptorsOfAnyShapeDoNotBreakTheMovie() {
        val movie = tmdbJson.decodeFromString<TmdbMovieDto>(
            """{"id": 5, "release_dates": {"results": [{"iso_3166_1": "IT", "release_dates": [
                {"certification": "14+", "type": 3, "release_date": "2024-01-01T00:00:00.000Z", "descriptors": ["Violence", {"id": 3, "name": "Weapons"}, 7]}
            ]}]}}"""
        )

        val date = movie.releaseDates!!.results.single().releaseDates.single()
        assertThat(date.descriptors.size).isEqualTo(3)
    }
}
