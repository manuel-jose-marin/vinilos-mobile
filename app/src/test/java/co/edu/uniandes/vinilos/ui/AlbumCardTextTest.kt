package co.edu.uniandes.vinilos.ui

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Performer
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumCardTextTest {

    private fun album(performers: List<String> = emptyList(), releaseDate: String = "1984-08-01T00:00:00.000Z", genre: String = "Salsa") =
        Album(
            id = 1, name = "Buscando América", cover = "", releaseDate = releaseDate, description = "",
            genre = genre, recordLabel = "Elektra",
            performers = performers.mapIndexed { i, name -> Performer(i, name, "", "") },
            tracks = emptyList(), comments = emptyList()
        )

    @Test
    fun artists_joinsPerformerNamesWithComma() {
        assertEquals("Rubén Blades", AlbumCardText.artists(album(listOf("Rubén Blades"))))
        assertEquals("Rubén Blades, Willie Colón", AlbumCardText.artists(album(listOf("Rubén Blades", "Willie Colón"))))
    }

    @Test
    fun artists_withoutPerformers_isEmpty() {
        assertEquals("", AlbumCardText.artists(album()))
    }

    @Test
    fun yearAndGenre_showsYearAndGenreSeparatedByDot() {
        assertEquals("1984 · Salsa", AlbumCardText.yearAndGenre(album()))
    }

    @Test
    fun yearAndGenre_omitsMissingValues() {
        assertEquals("Salsa", AlbumCardText.yearAndGenre(album(releaseDate = "")))
        assertEquals("1984", AlbumCardText.yearAndGenre(album(genre = "")))
    }
}
