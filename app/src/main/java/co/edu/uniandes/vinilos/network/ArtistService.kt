package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistPrize
import co.edu.uniandes.vinilos.models.ArtistType
import org.json.JSONArray
import org.json.JSONObject

/** Acceso a los músicos y bandas de BackVynils. Los errores de red o del servidor se propagan como excepción. */
interface ArtistService {
    /** `GET /musicians`: músicos con sus álbumes. */
    suspend fun getMusicians(): List<Artist>

    /** `GET /bands`: bandas con sus álbumes e integrantes. */
    suspend fun getBands(): List<Artist>

    /** `GET /musicians/{id}`: un músico con sus álbumes y premios. */
    suspend fun getMusician(id: Int): Artist

    /** `GET /bands/{id}`: una banda con sus álbumes, integrantes y premios. */
    suspend fun getBand(id: Int): Artist
}

class NetworkArtistService(private val network: NetworkServiceAdapter) : ArtistService {

    override suspend fun getMusicians(): List<Artist> = parseMusicians(network.getArray("musicians"))

    override suspend fun getBands(): List<Artist> = parseBands(network.getArray("bands"))

    override suspend fun getMusician(id: Int): Artist = parseMusician(network.getObject("musicians/$id"))

    override suspend fun getBand(id: Int): Artist = parseBand(network.getObject("bands/$id"))

    companion object {
        fun parseMusicians(json: JSONArray): List<Artist> = json.mapObjects { parseMusician(it) }

        fun parseBands(json: JSONArray): List<Artist> = json.mapObjects { parseBand(it) }

        fun parseMusician(json: JSONObject) = Artist(
            id = json.getInt("id"),
            name = json.getString("name"),
            image = json.optString("image"),
            description = json.optString("description"),
            type = ArtistType.MUSICIAN,
            date = json.optString("birthDate"),
            albums = json.optJSONArray("albums").mapObjects { NetworkAlbumService.parseAlbum(it) },
            members = emptyList(),
            prizes = parsePrizes(json)
        )

        fun parseBand(json: JSONObject) = Artist(
            id = json.getInt("id"),
            name = json.getString("name"),
            image = json.optString("image"),
            description = json.optString("description"),
            type = ArtistType.BAND,
            date = json.optString("creationDate"),
            albums = json.optJSONArray("albums").mapObjects { NetworkAlbumService.parseAlbum(it) },
            members = json.optJSONArray("musicians").mapObjects { JsonParsing.parsePerformer(it) },
            prizes = parsePrizes(json)
        )

        /** `performerPrizes` solo trae id y fecha; el nombre y la organización se completan con `GET /prizes`. */
        private fun parsePrizes(json: JSONObject): List<ArtistPrize> =
            json.optJSONArray("performerPrizes").mapObjects {
                ArtistPrize(id = it.getInt("id"), premiationDate = it.optString("premiationDate"))
            }
    }
}
