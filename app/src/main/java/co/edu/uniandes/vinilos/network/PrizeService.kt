package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Prize
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los premios de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface PrizeService {
    /** `GET /prizes`: premios con los ids de cada vez que se otorgaron (`performerPrizes`). */
    suspend fun getPrizes(): List<Prize>
}

class NetworkPrizeService(private val network: NetworkServiceAdapter) : PrizeService {

    override suspend fun getPrizes(): List<Prize> = parsePrizes(network.getArray("prizes"))

    companion object {
        fun parsePrizes(json: JSONArray): List<Prize> = json.mapObjects { parsePrize(it) }

        fun parsePrize(json: JSONObject) = Prize(
            id = json.getInt("id"),
            name = json.getString("name"),
            organization = json.optString("organization"),
            performerPrizeIds = json.optJSONArray("performerPrizes").mapObjects { it.getInt("id") }
        )
    }
}
