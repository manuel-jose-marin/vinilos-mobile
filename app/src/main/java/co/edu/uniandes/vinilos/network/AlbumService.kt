package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Track
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los álbumes de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface AlbumService {
    /** `GET /albums`: catálogo completo de álbumes. */
    suspend fun getAlbums(): List<Album>

    /** `GET /albums/{id}`: un álbum con sus artistas, tracks y comentarios. */
    suspend fun getAlbum(id: Int): Album
}

/**
 * Implementación sobre la API REST. La petición la hace Volley en su propio hilo y la conversión de JSON a
 * modelos corre en [parsing] (por defecto `Dispatchers.Default`), nunca en el hilo principal.
 */
class NetworkAlbumService(
    private val network: NetworkServiceAdapter,
    private val parsing: CoroutineDispatcher = Dispatchers.Default
) : AlbumService {

    override suspend fun getAlbums(): List<Album> = withContext(parsing) { parseAlbums(network.getArray("albums")) }

    override suspend fun getAlbum(id: Int): Album = withContext(parsing) { parseAlbum(network.getObject("albums/$id")) }

    companion object {
        fun parseAlbums(json: JSONArray): List<Album> = json.mapObjects { parseAlbum(it) }

        fun parseAlbum(json: JSONObject): Album = Album(
            id = json.getInt("id"),
            name = json.getString("name"),
            cover = json.optString("cover"),
            releaseDate = json.optString("releaseDate"),
            description = json.optString("description"),
            genre = json.optString("genre"),
            recordLabel = json.optString("recordLabel"),
            performers = json.optJSONArray("performers").mapObjects { JsonParsing.parsePerformer(it) },
            tracks = json.optJSONArray("tracks").mapObjects { parseTrack(it) },
            comments = json.optJSONArray("comments").mapObjects { JsonParsing.parseComment(it) }
        )

        private fun parseTrack(json: JSONObject) = Track(
            id = json.getInt("id"),
            name = json.getString("name"),
            duration = json.optString("duration")
        )
    }
}
