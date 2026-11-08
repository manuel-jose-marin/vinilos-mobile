package co.edu.uniandes.vinilos.network

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrizeServiceTest {

    /** Respuesta de `GET /prizes` con la forma que devuelve BackVynils. */
    private val prizes = """
        [
          {
            "id": 100,
            "organization": "Sony Music",
            "name": "Premio Grammy",
            "description": "Premio de la industria musical",
            "performerPrizes": [
              {"id": 100, "premiationDate": "1978-12-10T00:00:00.000Z"},
              {"id": 101, "premiationDate": "1985-02-26T00:00:00.000Z"}
            ]
          },
          {"id": 101, "organization": "Billboard", "name": "Billboard Music Award"}
        ]
    """.trimIndent()

    @Test
    fun parsePrizes_readsNameOrganizationAndPerformerPrizeIds() {
        val result = NetworkPrizeService.parsePrizes(JSONArray(prizes))

        assertEquals(listOf(100, 101), result.map { it.id })
        assertEquals("Premio Grammy", result[0].name)
        assertEquals("Sony Music", result[0].organization)
        assertEquals(listOf(100, 101), result[0].performerPrizeIds)
    }

    @Test
    fun parsePrizes_withoutPerformerPrizes_returnsEmptyIds() {
        val result = NetworkPrizeService.parsePrizes(JSONArray(prizes))

        assertTrue(result[1].performerPrizeIds.isEmpty())
    }

    @Test
    fun parsePrizes_emptyArray_returnsEmptyList() {
        assertTrue(NetworkPrizeService.parsePrizes(JSONArray()).isEmpty())
    }
}
