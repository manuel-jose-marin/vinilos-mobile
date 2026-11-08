package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistType
import co.edu.uniandes.vinilos.network.ArtistService

/**
 * Servicio de artistas para pruebas: devuelve lo que indiquen [musicians] y [bands] (por defecto, ninguno)
 * y cuenta cuántas veces se pidió el listado. El detalle busca el artista por id en esas mismas listas.
 */
class FakeArtistService(
    var musicians: suspend () -> List<Artist> = { emptyList() },
    var bands: suspend () -> List<Artist> = { emptyList() }
) : ArtistService {
    var musicianCalls = 0
        private set
    var bandCalls = 0
        private set

    override suspend fun getMusicians(): List<Artist> {
        musicianCalls++
        return musicians()
    }

    override suspend fun getBands(): List<Artist> {
        bandCalls++
        return bands()
    }

    override suspend fun getMusician(id: Int): Artist = musicians().first { it.id == id }

    override suspend fun getBand(id: Int): Artist = bands().first { it.id == id }

    companion object {
        fun artist(id: Int, name: String, type: ArtistType) =
            Artist(id, name, "", "", type, "", emptyList(), emptyList())
    }
}
