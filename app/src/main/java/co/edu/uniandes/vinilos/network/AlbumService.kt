package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.NewTrack
import co.edu.uniandes.vinilos.models.Track
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los álbumes de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface AlbumService {
    /** `GET /albums`: catálogo completo de álbumes. */
    suspend fun getAlbums(): List<Album>

    /** `GET /albums/{id}`: un álbum con sus artistas, tracks y comentarios. */
    suspend fun getAlbum(id: Int): Album

    /** `POST /albums/{id}/tracks`: asocia un nuevo track a un álbum existente (HU08). */
    suspend fun addTrack(albumId: Int, track: NewTrack): Track
}

class NetworkAlbumService(private val network: NetworkServiceAdapter) : AlbumService {

    override suspend fun getAlbums(): List<Album> =
        parseOffMainThread(network.getArray("albums")) { parseAlbums(it) }

    override suspend fun getAlbum(id: Int): Album =
        parseOffMainThread(network.getObject("albums/$id")) { parseAlbum(it) }

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track =
        parseOffMainThread(network.post("albums/$albumId/tracks", track.toJson())) { parseTrack(it) }

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

        fun parseTrack(json: JSONObject) = Track(
            id = json.getInt("id"),
            name = json.getString("name"),
            duration = json.optString("duration")
        )
    }
}
