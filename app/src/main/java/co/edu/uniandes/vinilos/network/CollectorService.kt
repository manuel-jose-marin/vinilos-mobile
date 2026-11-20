package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.models.CollectorAlbum
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los coleccionistas de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface CollectorService {
    /** `GET /collectors`: coleccionistas con sus comentarios y el número de álbumes de su colección. */
    suspend fun getCollectors(): List<Collector>

    /** `GET /collectors/{id}`: un coleccionista con sus comentarios y sus artistas favoritos (HU06). */
    suspend fun getCollector(id: Int): Collector

    /** `GET /collectors/{id}/albums`: álbumes de su colección con precio y estado, sin el artista (HU06). */
    suspend fun getCollectorAlbums(id: Int): List<CollectorAlbum>
}

class NetworkCollectorService(private val network: NetworkServiceAdapter) : CollectorService {

    override suspend fun getCollectors(): List<Collector> =
        parseOffMainThread(network.getArray("collectors")) { parseCollectors(it) }

    // La petición y el parseo del detalle se implementan en #50 (servicio REST del detalle de coleccionista).
    override suspend fun getCollector(id: Int): Collector =
        TODO("#50: GET /collectors/{id}")

    override suspend fun getCollectorAlbums(id: Int): List<CollectorAlbum> =
        TODO("#50: GET /collectors/{id}/albums")

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
