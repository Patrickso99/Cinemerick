package com.preichert.cinemerick.feature.showtimes.data

import com.preichert.cinemerick.feature.showtimes.domain.FilmDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// The markup below is condensed from real film pages of each chain.
class FilmPageParsersTest {

    private val notoriousPage = """
        <div id="boxLocandina">
          <img src="../cvu/modules/img_switcher.php?idfilm=48721&type=446&ext=jpg" border="0">
          <div class="box_trailer"><a class="linkTrailer linkExt" href="http://www.youtrailer.com/trailer/48721/7d8dc488afaf80c533d072b7af5138adB10ok3HG7OyFiVo8/0/"><i class="fas fa-chevron-right fa-lg"></i></a></div>
        </div>
        <section class="trailer_scheda"><img src="https://img.youtube.com/vi/3HG7OyFiVo8/maxresdefault.jpg" alt="trailer placeholder" /></section>
        <div id="titolo"><h1>L'ALBERO DI MOLTO LONTANO</h1></div>
        <div id="dati">
          <strong>Regia: </strong>Ben Gregor<br /><strong>Cast: </strong>Andrew Garfield, Claire Foy, Nicola Coughlan<br /><strong>Durata: </strong>110'<br /><strong>Genere: </strong>Avventura, Family<br /><strong>Nazione e Anno: </strong>Gran Bretagna, 2026
        </div>
        <div id="trama">Subito dopo l'arrivo della famiglia Thompson in campagna, i tre figli scoprono un albero magico.</div>
    """.trimIndent()

    private val arcadiaPage = """
        <div class="btt_reel"><a href="https://www.youtube.com/watch?v=gUth3APCeyg" title=" - trailer" target="_blank"></a></div>
        <div id="scheda">
          <div id="boxLocandina"><img src="/cdn/movies/a.jpg" alt="AVENGERS - DOOMSDAY"/></div>
          <div id="info_film">
            <div class="titolo"><h1>AVENGERS - DOOMSDAY</h1></div>
            <div class="regia"><span class="label">Regia:</span> Anthony Russo, Joe Russo</div>
            <div class="genere"><span class="label">Genere:</span> Azione, Fantascienza</div>
            <div class="durata"><span class="label">Durata:</span> 165'</div>
            <div class="cast"><span class="label">Cast:</span> Joseph Quinn, Pedro Pascal</div>
            <div id="trama"><p>APERTURA PREVENDITE<br />
        dalle ore 00:01 di LUNED&Igrave; 5 OTTOBRE</p>

        <p>Gli universi si scontrano in Avengers: Doomsday dei Marvel Studios.</p>
        </div>
          </div>
          <div id="preno"></div>
        </div>
    """.trimIndent()

    private val cristalloPage = """
        <ul class="info-list">
          <li><label>
                Regia:
            </label><span><a href="https://x/director/yuval-abraham/">Yuval Abraham</a>, <a href="https://x/director/rachel-szor/">Rachel Szor</a></span></li>
          <li><label>
                Genere:
            </label><span><a href="https://x/genre/documentario/">Documentario</a></span></li>
        </ul>
        <div class="entry-content e-content" itemprop="description articleBody">
          <h3 class="info-name amy-title">Trama</h3>
          <p>Girato di notte sui tetti di Tel Aviv, il film rivela i sistemi di Gaza.</p>
        <p><em>I registi:</em> Spetta a tutti noi rompere quel silenzio, dell&rsquo;uccisione.</p>
        <p><strong>Paese:</strong> Gran Bretagna, USA<br>
        <strong>Distribuzione:</strong> I Wonder Pictures</p>
        </div>
    """.trimIndent()

    private val ticketsPage = """
        <a class='trailer-sample btn btn btn-md btn--warning btn-block mt-2 mb-2' href='https://www.youtube.com/watch?v=l2iJmznTVNI'>
        <i class='fa fa-youtube mr-1'></i>
        Guarda il trailer
        </a>
        <p class='movie__time'>
        Durata: 115 min
        </p>
        <p class='movie__option'>
        <strong>
        Genere:
        </strong>
        Thriller, Dramma
        </p>
        <p class='movie__option'>
        <strong>
        Età
        </strong>
        6
        </p>
        <p class='movie__option'>
        <strong>
        Regia:
        </strong>
        Paolo Strippoli
        </p>
        <p class='movie__option'>
        <strong>
        Con:
        </strong>
        Jasmine Trinca, Romana Maggiora Vergano
        </p>
        <div aria-labelledby='plotHeader' class='collapse' data-parent='#accordion' id='plotPanel'>
        <div class='card-body'>
        La trama ruota attorno a una donna che si presenta all’improvviso a casa di sua figlia.
        <br>
        </div>
        </div>
    """.trimIndent()

    @Test
    fun notoriousPageGivesPlotCreditsAndYoutubeTrailer() {
        assertEquals(
            FilmDetails(
                synopsis = "Subito dopo l'arrivo della famiglia Thompson in campagna, i tre figli scoprono un albero magico.",
                genre = "Avventura, Family",
                director = "Ben Gregor",
                cast = "Andrew Garfield, Claire Foy, Nicola Coughlan",
                trailerUrl = "https://www.youtube.com/watch?v=3HG7OyFiVo8"
            ),
            parseNotoriousFilmPage(notoriousPage)
        )
    }

    @Test
    fun notoriousTrailerFallsBackToTheButtonLinkWithoutThumbnail() {
        val page = notoriousPage.replace("img.youtube.com/vi/3HG7OyFiVo8/", "example.com/")

        assertEquals(
            "http://www.youtrailer.com/trailer/48721/7d8dc488afaf80c533d072b7af5138adB10ok3HG7OyFiVo8/0/",
            parseNotoriousFilmPage(page).trailerUrl
        )
    }

    @Test
    fun notoriousPageWithoutTrailerButtonHasNoTrailer() {
        assertNull(parseNotoriousFilmPage(notoriousPage.replace("box_trailer", "other")).trailerUrl)
    }

    @Test
    fun arcadiaPageGivesPlotAndCredits() {
        val details = parseArcadiaFilmPage(arcadiaPage)

        assertEquals("Anthony Russo, Joe Russo", details.director)
        assertEquals("Azione, Fantascienza", details.genre)
        assertEquals("Joseph Quinn, Pedro Pascal", details.cast)
        assertEquals(
            "APERTURA PREVENDITE\ndalle ore 00:01 di LUNEDÌ 5 OTTOBRE\nGli universi si scontrano in Avengers: Doomsday dei Marvel Studios.",
            details.synopsis
        )
    }

    @Test
    fun arcadiaIgnoresTheTrailersOfOtherFilmsInThePageHeader() {
        assertNull(parseArcadiaFilmPage(arcadiaPage).trailerUrl)
    }

    @Test
    fun arcadiaTakesATrailerFromTheFilmsOwnSection() {
        val page = arcadiaPage.replace("""<div id="preno">""", """<a href="https://www.youtube.com/watch?v=abcDEF12345">trailer</a><div id="preno">""")

        assertEquals("https://www.youtube.com/watch?v=abcDEF12345", parseArcadiaFilmPage(page).trailerUrl)
    }

    @Test
    fun cristalloPageGivesPlotDirectorsAndGenreWithoutCredits() {
        val details = parseCristalloFilmPage(cristalloPage)

        assertEquals("Yuval Abraham, Rachel Szor", details.director)
        assertEquals("Documentario", details.genre)
        assertEquals(
            "Girato di notte sui tetti di Tel Aviv, il film rivela i sistemi di Gaza.\nI registi: Spetta a tutti noi rompere quel silenzio, dell’uccisione.",
            details.synopsis
        )
        assertNull(details.cast)
        assertNull(details.trailerUrl)
    }

    @Test
    fun eighteenTicketsPageGivesPlotCreditsAgeAndTrailer() {
        assertEquals(
            FilmDetails(
                synopsis = "La trama ruota attorno a una donna che si presenta all’improvviso a casa di sua figlia.",
                genre = "Thriller, Dramma",
                director = "Paolo Strippoli",
                cast = "Jasmine Trinca, Romana Maggiora Vergano",
                rating = "6+",
                trailerUrl = "https://www.youtube.com/watch?v=l2iJmznTVNI"
            ),
            parseEighteenTicketsFilmPage(ticketsPage)
        )
    }

    @Test
    fun pageWithoutKnownMarkupGivesEmptyDetails() {
        val html = "<html><body>404</body></html>"

        assertTrue(parseNotoriousFilmPage(html).isEmpty)
        assertTrue(parseArcadiaFilmPage(html).isEmpty)
        assertTrue(parseCristalloFilmPage(html).isEmpty)
        assertTrue(parseEighteenTicketsFilmPage(html).isEmpty)
    }

    @Test
    fun eighteenTicketsRatingComesFromTheClassificationIconWhenThereIsNoAgeLine() {
        val everyone = "<img src='https://cdn/content_classification_icons/0.png' title='Opere per tutti'>"
        val minors = "<img src='https://cdn/content_classification_icons/14.png' title='Opere non adatte ai minori di anni 14'>"

        assertEquals("Per tutti", parseEighteenTicketsFilmPage(everyone).rating)
        assertEquals("14+", parseEighteenTicketsFilmPage(minors).rating)
    }
}
