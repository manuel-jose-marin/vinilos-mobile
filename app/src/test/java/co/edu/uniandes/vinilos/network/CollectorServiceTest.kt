package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.ArtistType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectorServiceTest {

    private val fullCollector = """
        {
          "id": 100,
          "name": "Manolo Bellon",
          "telephone": "3502457896",
          "email": "manollo@caracol.com.co",
          "comments": [
            {"id": 100, "description": "El más relevante", "rating": 5}
          ],
          "favoritePerformers": [],
          "collectorAlbums": [
            {"id": 100, "price": 25000, "status": "Active"},
            {"id": 101, "price": 30000, "status": "Inactive"}
          ]
        }
    """.trimIndent()

    @Test
    fun parseCollector_readsAllFieldsAndAlbumCount() {
        val collector = NetworkCollectorService.parseCollector(JSONObject(fullCollector))

        assertEquals(100, collector.id)
        assertEquals("Manolo Bellon", collector.name)
        assertEquals("3502457896", collector.telephone)
        assertEquals("manollo@caracol.com.co", collector.email)
        assertEquals(5, collector.comments.single().rating)
        assertEquals(2, collector.albumCount)
    }

    @Test
    fun parseCollector_withoutCommentsOrAlbums_returnsEmptyListAndZero() {
        val json = JSONObject("""{"id": 7, "name": "Sin colección", "telephone": "", "email": ""}""")

        val collector = NetworkCollectorService.parseCollector(json)

        assertTrue(collector.comments.isEmpty())
        assertEquals(0, collector.albumCount)
    }

    @Test
    fun parseCollectors_keepsServerOrder() {
        val json = JSONArray(
            """[{"id": 1, "name": "Ana", "telephone": "", "email": ""},
                {"id": 2, "name": "Beto", "telephone": "", "email": ""}]"""
        )

        assertEquals(listOf("Ana", "Beto"), NetworkCollectorService.parseCollectors(json).map { it.name })
    }

    @Test
    fun parseCollectors_emptyArray_returnsEmptyList() {
        assertTrue(NetworkCollectorService.parseCollectors(JSONArray()).isEmpty())
    }

    // Detalle (HU06)

    /** Respuesta de `GET /collectors/{id}` con un músico y una banda entre los favoritos. */
    private val collectorWithFavorites = """
        {
          "id": 100,
          "name": "Manolo Bellon",
          "telephone": "3502457896",
          "email": "manollo@caracol.com.co",
          "comments": [],
          "favoritePerformers": [
            {"id": 100, "name": "Rubén Blades Bellido de Luna", "image": "https://ruben.jpg",
             "description": "Cantante panameño", "birthDate": "1948-07-16T00:00:00.000Z"},
            {"id": 101, "name": "Queen", "image": "https://queen.jpg",
             "description": "Banda británica", "creationDate": "1970-01-01T00:00:00.000Z"}
          ],
          "collectorAlbums": []
        }
    """.trimIndent()

    @Test
    fun parseCollector_readsFavoritePerformers() {
        val favorites = NetworkCollectorService.parseCollector(JSONObject(collectorWithFavorites)).favoritePerformers

        assertEquals(listOf("Rubén Blades Bellido de Luna", "Queen"), favorites.map { it.name })
        assertEquals("https://ruben.jpg", favorites[0].image)
    }

    @Test
    fun parseCollector_favoriteWithBirthDate_isMusician() {
        val favorites = NetworkCollectorService.parseCollector(JSONObject(collectorWithFavorites)).favoritePerformers

        assertEquals(ArtistType.MUSICIAN, favorites[0].type)
    }

    @Test
    fun parseCollector_favoriteWithCreationDate_isBand() {
        val favorites = NetworkCollectorService.parseCollector(JSONObject(collectorWithFavorites)).favoritePerformers

        assertEquals(ArtistType.BAND, favorites[1].type)
    }

    @Test
    fun parseCollector_favoriteWithoutDates_hasNoType() {
        val json = JSONObject(
            """{"id": 1, "name": "Ana", "favoritePerformers": [{"id": 5, "name": "Sin fecha", "birthDate": null}]}"""
        )

        assertNull(NetworkCollectorService.parseCollector(json).favoritePerformers.single().type)
    }

    @Test
    fun parseCollector_withoutFavorites_returnsEmptyList() {
        val json = JSONObject("""{"id": 7, "name": "Sin favoritos", "telephone": "", "email": ""}""")

        assertTrue(NetworkCollectorService.parseCollector(json).favoritePerformers.isEmpty())
    }

    /** Respuesta de `GET /collectors/{id}/albums`: cada álbum de la colección con precio, estado y el álbum. */
    private val collectorAlbums = """
        [
          {"id": 100, "price": 25000, "status": "Active",
           "album": {"id": 100, "name": "Buscando América", "cover": "https://buscando.jpg",
                     "releaseDate": "1984-08-01T00:00:00.000Z", "description": "Primer álbum",
                     "genre": "Salsa", "recordLabel": "Elektra"},
           "collector": {"id": 100, "name": "Manolo Bellon"}},
          {"id": 101, "price": 30000, "status": "Inactive",
           "album": {"id": 101, "name": "Poeta del pueblo", "cover": "https://poeta.jpg",
                     "releaseDate": "1984-08-01T00:00:00.000Z", "description": "Segundo álbum",
                     "genre": "Salsa", "recordLabel": "Elektra"},
           "collector": {"id": 100, "name": "Manolo Bellon"}}
        ]
    """.trimIndent()

    @Test
    fun parseCollectorAlbums_readsPriceStatusAndAlbum() {
        val albums = NetworkCollectorService.parseCollectorAlbums(JSONArray(collectorAlbums))

        assertEquals(2, albums.size)
        assertEquals(100, albums[0].id)
        assertEquals(25000, albums[0].price)
        assertTrue(albums[0].isAvailable)
        assertFalse(albums[1].isAvailable)
        assertEquals("Buscando América", albums[0].album.name)
        assertEquals("https://buscando.jpg", albums[0].album.cover)
        assertEquals("1984-08-01T00:00:00.000Z", albums[0].album.releaseDate)
    }

    @Test
    fun parseCollectorAlbums_albumComesWithoutPerformers() {
        val albums = NetworkCollectorService.parseCollectorAlbums(JSONArray(collectorAlbums))

        assertTrue(albums.all { it.album.performers.isEmpty() })
    }

    @Test
    fun parseCollectorAlbums_emptyArray_returnsEmptyList() {
        assertTrue(NetworkCollectorService.parseCollectorAlbums(JSONArray()).isEmpty())
    }
}
