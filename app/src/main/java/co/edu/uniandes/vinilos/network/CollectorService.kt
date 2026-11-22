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

    override suspend fun getCollector(id: Int): Collector =
        parseOffMainThread(network.getObject("collectors/$id")) { parseCollector(it) }

    override suspend fun getCollectorAlbums(id: Int): List<CollectorAlbum> =
        parseOffMainThread(network.getArray("collectors/$id/albums")) { parseCollectorAlbums(it) }

    companion object {
        fun parseCollectors(json: JSONArray): List<Collector> = json.mapObjects { parseCollector(it) }

        fun parseCollector(json: JSONObject) = Collector(
            id = json.getInt("id"),
            name = json.getString("name"),
            telephone = json.optString("telephone"),
            email = json.optString("email"),
            comments = json.optJSONArray("comments").mapObjects { JsonParsing.parseComment(it) },
            albumCount = json.optJSONArray("collectorAlbums")?.length() ?: 0,
            favoritePerformers = json.optJSONArray("favoritePerformers")
                .mapObjects { JsonParsing.parseFavoritePerformer(it) }
        )

        fun parseCollectorAlbums(json: JSONArray): List<CollectorAlbum> = json.mapObjects { parseCollectorAlbum(it) }

        /** Álbum de la colección: precio, estado y el álbum, que BackVynils envía sin sus artistas. */
        fun parseCollectorAlbum(json: JSONObject) = CollectorAlbum(
            id = json.getInt("id"),
            price = json.optInt("price"),
            status = json.optString("status"),
            album = NetworkAlbumService.parseAlbum(json.getJSONObject("album"))
        )
    }
}
