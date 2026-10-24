package co.edu.uniandes.vinilos.network

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumServiceTest {

    private val fullAlbum = """
        {
          "id": 100,
          "name": "Buscando América",
          "cover": "https://example.com/cover.jpg",
          "releaseDate": "1984-08-01T00:00:00.000Z",
          "description": "Álbum de salsa",
          "genre": "Salsa",
          "recordLabel": "Elektra",
          "performers": [
            {"id": 100, "name": "Rubén Blades", "image": "https://example.com/ruben.jpg", "description": "Cantante panameño"}
          ],
          "tracks": [
            {"id": 100, "name": "Decisiones", "duration": "5:05"},
            {"id": 101, "name": "Desapariciones", "duration": "6:29"}
          ],
          "comments": [
            {"id": 100, "description": "Excelente álbum", "rating": 5}
          ]
        }
    """.trimIndent()

    @Test
    fun parseAlbum_readsAllFieldsAndRelations() {
        val album = NetworkAlbumService.parseAlbum(JSONObject(fullAlbum))

        assertEquals(100, album.id)
        assertEquals("Buscando América", album.name)
        assertEquals("https://example.com/cover.jpg", album.cover)
        assertEquals("Salsa", album.genre)
        assertEquals("Elektra", album.recordLabel)
        assertEquals("1984", album.releaseYear)
        assertEquals("Rubén Blades", album.performers.single().name)
        assertEquals(listOf("Decisiones", "Desapariciones"), album.tracks.map { it.name })
        assertEquals("5:05", album.tracks.first().duration)
        assertEquals(5, album.comments.single().rating)
    }

    @Test
    fun parseAlbum_withoutTracksOrComments_returnsEmptyLists() {
        val json = JSONObject(
            """{"id": 7, "name": "Sin extras", "cover": "", "releaseDate": "2000-01-01T00:00:00.000Z",
                "description": "", "genre": "Rock", "recordLabel": "Sony Music"}"""
        )

        val album = NetworkAlbumService.parseAlbum(json)

        assertTrue(album.performers.isEmpty())
        assertTrue(album.tracks.isEmpty())
        assertTrue(album.comments.isEmpty())
    }

    @Test
    fun parseAlbums_keepsServerOrder() {
        val json = JSONArray().put(JSONObject(fullAlbum)).put(JSONObject(fullAlbum).put("id", 101))

        val albums = NetworkAlbumService.parseAlbums(json)

        assertEquals(2, albums.size)
        assertEquals(listOf(100, 101), albums.map { it.id })
    }

    @Test
    fun parseAlbums_emptyArray_returnsEmptyList() {
        assertTrue(NetworkAlbumService.parseAlbums(JSONArray("[]")).isEmpty())
    }
}
