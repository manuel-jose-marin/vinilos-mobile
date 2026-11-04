package co.edu.uniandes.vinilos.network

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
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
}
