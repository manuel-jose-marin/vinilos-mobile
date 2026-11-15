package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Prize
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los premios de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface PrizeService {
    /** `GET /prizes`: premios con los ids de cada vez que se otorgaron (`performerPrizes`). */
    suspend fun getPrizes(): List<Prize>
}

/**
 * Implementación sobre la API REST. La petición la hace Volley en su propio hilo y la conversión de JSON a
 * modelos corre en [parsing] (por defecto `Dispatchers.Default`), nunca en el hilo principal.
 */
class NetworkPrizeService(
    private val network: NetworkServiceAdapter,
    private val parsing: CoroutineDispatcher = Dispatchers.Default
) : PrizeService {

    override suspend fun getPrizes(): List<Prize> = withContext(parsing) { parsePrizes(network.getArray("prizes")) }

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
