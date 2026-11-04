package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.ArtistType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtistServiceTest {

    private val fullMusician = """
        {
          "id": 100,
          "name": "Rubén Blades Bellido de Luna",
          "image": "https://example.com/ruben.jpg",
          "description": "Cantante panameño",
          "birthDate": "1948-07-16T00:00:00.000Z",
          "albums": [
            {"id": 100, "name": "Buscando América", "cover": "https://example.com/cover.jpg",
             "releaseDate": "1984-08-01T00:00:00.000Z", "description": "Álbum de salsa",
             "genre": "Salsa", "recordLabel": "Elektra"},
            {"id": 101, "name": "Poeta del pueblo", "cover": "https://example.com/poeta.jpg",
             "releaseDate": "1984-08-01T00:00:00.000Z", "description": "Recopilación",
             "genre": "Salsa", "recordLabel": "Elektra"}
          ],
          "performerPrizes": [{"id": 100, "premiationDate": "1978-12-10T00:00:00.000Z"}]
        }
    """.trimIndent()

    private val fullBand = """
        {
          "id": 101,
          "name": "Queen",
          "image": "https://example.com/queen.jpg",
          "description": "Banda británica de rock",
          "creationDate": "1970-01-01T00:00:00.000Z",
          "albums": [
            {"id": 102, "name": "A Night at the Opera", "cover": "https://example.com/opera.png",
             "releaseDate": "1975-11-21T00:00:00.000Z", "description": "Cuarto álbum de estudio",
             "genre": "Rock", "recordLabel": "EMI"}
          ],
          "musicians": [
            {"id": 200, "name": "Freddie Mercury", "image": "https://example.com/freddie.jpg",
             "description": "Vocalista", "birthDate": "1946-09-05T00:00:00.000Z"}
          ],
          "performerPrizes": []
        }
    """.trimIndent()

    @Test
    fun parseMusician_readsAllFieldsAndHasNoMembers() {
        val artist = NetworkArtistService.parseMusician(JSONObject(fullMusician))

        assertEquals(100, artist.id)
        assertEquals("Rubén Blades Bellido de Luna", artist.name)
        assertEquals("https://example.com/ruben.jpg", artist.image)
        assertEquals("Cantante panameño", artist.description)
        assertEquals(ArtistType.MUSICIAN, artist.type)
        assertEquals("1948-07-16T00:00:00.000Z", artist.date)
        assertEquals(listOf("Buscando América", "Poeta del pueblo"), artist.albums.map { it.name })
        assertTrue(artist.members.isEmpty())
    }

    @Test
    fun parseBand_readsCreationDateAndMembers() {
        val artist = NetworkArtistService.parseBand(JSONObject(fullBand))

        assertEquals(101, artist.id)
        assertEquals("Queen", artist.name)
        assertEquals(ArtistType.BAND, artist.type)
        assertEquals("1970-01-01T00:00:00.000Z", artist.date)
        assertEquals("A Night at the Opera", artist.albums.single().name)
        assertEquals("Freddie Mercury", artist.members.single().name)
    }

    @Test
    fun parseMusician_withoutAlbums_returnsEmptyList() {
        val artist = NetworkArtistService.parseMusician(JSONObject("""{"id": 7, "name": "Sin álbumes"}"""))

        assertTrue(artist.albums.isEmpty())
    }

    @Test
    fun parseBand_withoutAlbumsOrMembers_returnsEmptyLists() {
        val artist = NetworkArtistService.parseBand(JSONObject("""{"id": 8, "name": "Sin integrantes"}"""))

        assertTrue(artist.albums.isEmpty())
        assertTrue(artist.members.isEmpty())
    }

    @Test
    fun parseMusicians_assignsMusicianTypeAndKeepsServerOrder() {
        val artists = NetworkArtistService.parseMusicians(
            JSONArray("""[{"id": 1, "name": "Zoe"}, {"id": 2, "name": "Ana"}]""")
        )

        assertEquals(listOf("Zoe", "Ana"), artists.map { it.name })
        assertTrue(artists.all { it.type == ArtistType.MUSICIAN })
    }

    @Test
    fun parseBands_assignsBandTypeAndKeepsServerOrder() {
        val artists = NetworkArtistService.parseBands(
            JSONArray("""[{"id": 3, "name": "Soda Stereo"}, {"id": 4, "name": "Aterciopelados"}]""")
        )

        assertEquals(listOf("Soda Stereo", "Aterciopelados"), artists.map { it.name })
        assertTrue(artists.all { it.type == ArtistType.BAND })
    }

    @Test
    fun parseMusicians_emptyArray_returnsEmptyList() {
        assertTrue(NetworkArtistService.parseMusicians(JSONArray()).isEmpty())
    }
}
