package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Comment
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
        fun parseAlbums(json: JSONArray): List<Album> = json.map { parseAlbum(it) }

        fun parseAlbum(json: JSONObject): Album = Album(
            id = json.getInt("id"),
            name = json.getString("name"),
            cover = json.optString("cover"),
            releaseDate = json.optString("releaseDate"),
            description = json.optString("description"),
            genre = json.optString("genre"),
            recordLabel = json.optString("recordLabel"),
            performers = json.optJSONArray("performers").map { parsePerformer(it) },
            tracks = json.optJSONArray("tracks").map { parseTrack(it) },
            comments = json.optJSONArray("comments").map { parseComment(it) }
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

        private fun parseComment(json: JSONObject) = Comment(
            id = json.getInt("id"),
            description = json.optString("description"),
            rating = json.optInt("rating")
        )

        /** Convierte un arreglo JSON en lista; si el servidor no lo envía (null) devuelve una lista vacía. */
        private fun <T> JSONArray?.map(transform: (JSONObject) -> T): List<T> =
            if (this == null) emptyList() else List(length()) { transform(getJSONObject(it)) }
    }
}
