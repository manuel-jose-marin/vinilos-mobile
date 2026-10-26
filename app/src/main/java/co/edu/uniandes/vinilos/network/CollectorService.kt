package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Collector
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los coleccionistas de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface CollectorService {
    /** `GET /collectors`: coleccionistas con sus comentarios. */
    suspend fun getCollectors(): List<Collector>
}

class NetworkCollectorService(private val network: NetworkServiceAdapter) : CollectorService {

    override suspend fun getCollectors(): List<Collector> = parseCollectors(network.getArray("collectors"))

    companion object {
        fun parseCollectors(json: JSONArray): List<Collector> = json.mapObjects { parseCollector(it) }

        fun parseCollector(json: JSONObject) = Collector(
            id = json.getInt("id"),
            name = json.getString("name"),
            telephone = json.optString("telephone"),
            email = json.optString("email"),
            comments = json.optJSONArray("comments").mapObjects { JsonParsing.parseComment(it) }
        )
    }
}
