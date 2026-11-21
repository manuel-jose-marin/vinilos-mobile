package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.NewAlbum
import co.edu.uniandes.vinilos.models.Performer
import co.edu.uniandes.vinilos.network.AlbumService

/**
 * Servicio de álbumes para pruebas: el catálogo es lo que indique [albums] (por defecto, vacío).
 * `createAlbum` responde lo que indique [created] (por defecto, el álbum con id 101) y guarda cada petición en
 * [createRequests].
 */
class FakeAlbumService(var albums: suspend () -> List<Album> = { emptyList() }) : AlbumService {

    var created: suspend (NewAlbum) -> Album = { album(101, it.name) }

    val createRequests = mutableListOf<NewAlbum>()

    override suspend fun getAlbums(): List<Album> = albums()

    override suspend fun getAlbum(id: Int): Album =
        albums().firstOrNull { it.id == id } ?: error("Álbum $id no configurado")

    override suspend fun createAlbum(album: NewAlbum): Album {
        createRequests += album
        return created(album)
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
