package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Performer
import co.edu.uniandes.vinilos.models.Track
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los álbumes de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface AlbumService {
    /** `GET /albums`: catálogo completo de álbumes. */
    suspend fun getAlbums(): List<Album>

    /** `GET /albums/{id}`: un álbum con sus artistas, tracks y comentarios. */
    suspend fun getAlbum(id: Int): Album
}

class NetworkAlbumService(private val network: NetworkServiceAdapter) : AlbumService {

    override suspend fun getAlbums(): List<Album> = parseAlbums(network.getArray("albums"))

    override suspend fun getAlbum(id: Int): Album = parseAlbum(network.getObject("albums/$id"))

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
            performers = json.optJSONArray("performers").mapObjects { parsePerformer(it) },
            tracks = json.optJSONArray("tracks").mapObjects { parseTrack(it) },
            comments = json.optJSONArray("comments").mapObjects { JsonParsing.parseComment(it) }
        )

        private fun parsePerformer(json: JSONObject) = Performer(
            id = json.getInt("id"),
            name = json.getString("name"),
            image = json.optString("image"),
            description = json.optString("description")
        )

        private fun parseTrack(json: JSONObject) = Track(
            id = json.getInt("id"),
            name = json.getString("name"),
            duration = json.optString("duration")
        )
    }
}
