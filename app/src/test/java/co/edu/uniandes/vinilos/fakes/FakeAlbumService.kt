package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.NewTrack
import co.edu.uniandes.vinilos.models.Performer
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.network.AlbumService

/** Servicio de álbumes para pruebas: el catálogo es lo que indique [albums] (por defecto, vacío). */
class FakeAlbumService(var albums: suspend () -> List<Album> = { emptyList() }) : AlbumService {

    override suspend fun getAlbums(): List<Album> = albums()

    override suspend fun getAlbum(id: Int): Album =
        albums().firstOrNull { it.id == id } ?: error("Álbum $id no configurado")

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track =
        Track(100, track.name, track.duration)

    companion object {
        /** Álbum con los datos mínimos para las pruebas. */
        fun album(id: Int, name: String, performers: List<Performer> = emptyList()) =
            Album(
                id = id, name = name, cover = "https://portada/$id.jpg", releaseDate = "1984-08-01T00:00:00.000Z",
                description = "", genre = "Salsa", recordLabel = "Elektra", performers = performers,
                tracks = emptyList(), comments = emptyList()
            )
    }
}
