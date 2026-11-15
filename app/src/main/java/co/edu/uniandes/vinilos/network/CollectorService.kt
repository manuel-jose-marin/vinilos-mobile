package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Collector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los coleccionistas de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface CollectorService {
    /** `GET /collectors`: coleccionistas con sus comentarios y el número de álbumes de su colección. */
    suspend fun getCollectors(): List<Collector>
}

/**
 * Implementación sobre la API REST. La petición la hace Volley en su propio hilo y la conversión de JSON a
 * modelos corre en [parsing] (por defecto `Dispatchers.Default`), nunca en el hilo principal.
 */
class NetworkCollectorService(
    private val network: NetworkServiceAdapter,
    private val parsing: CoroutineDispatcher = Dispatchers.Default
) : CollectorService {

    override suspend fun getCollectors(): List<Collector> =
        withContext(parsing) { parseCollectors(network.getArray("collectors")) }

    companion object {
        fun parseCollectors(json: JSONArray): List<Collector> = json.mapObjects { parseCollector(it) }

        fun parseCollector(json: JSONObject) = Collector(
            id = json.getInt("id"),
            name = json.getString("name"),
            telephone = json.optString("telephone"),
            email = json.optString("email"),
            comments = json.optJSONArray("comments").mapObjects { JsonParsing.parseComment(it) },
            albumCount = json.optJSONArray("collectorAlbums")?.length() ?: 0
        )
    }
}
