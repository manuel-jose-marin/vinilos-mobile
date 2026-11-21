package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.NewAlbum
import co.edu.uniandes.vinilos.models.NewTrack
import co.edu.uniandes.vinilos.models.Performer
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.network.AlbumService

/**
 * Servicio de álbumes para pruebas: el catálogo es lo que indique [albums] (por defecto, vacío).
 * `createAlbum` responde lo que indique [created] (por defecto, el álbum con id 101) y guarda cada petición en
 * [createRequests]. `addTrack` responde lo que indique [addedTrack] (por defecto, el track con id 100) y guarda cada
 * petición, con el id del álbum, en [trackRequests].
 */
class FakeAlbumService(var albums: suspend () -> List<Album> = { emptyList() }) : AlbumService {

    var created: suspend (NewAlbum) -> Album = { album(101, it.name) }

    val createRequests = mutableListOf<NewAlbum>()

    var addedTrack: suspend (Int, NewTrack) -> Track = { _, track -> Track(100, track.name, track.duration) }

    val trackRequests = mutableListOf<Pair<Int, NewTrack>>()

    override suspend fun getAlbums(): List<Album> = albums()

    override suspend fun getAlbum(id: Int): Album =
        albums().firstOrNull { it.id == id } ?: error("Álbum $id no configurado")

    override suspend fun createAlbum(album: NewAlbum): Album {
        createRequests += album
        return created(album)
    }

    override suspend fun addTrack(albumId: Int, track: NewTrack): Track {
        trackRequests += albumId to track
        return addedTrack(albumId, track)
    }

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
