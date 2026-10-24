package co.edu.uniandes.vinilos.repositories

import android.content.Context
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.network.AlbumService
import co.edu.uniandes.vinilos.network.NetworkAlbumService
import co.edu.uniandes.vinilos.network.NetworkServiceAdapter

/** Fuente de datos de álbumes para los ViewModels; oculta de dónde vienen (hoy, la API REST). */
class AlbumRepository(private val service: AlbumService) {

    /** Catálogo de álbumes. Si falla la red o el servidor, lanza la excepción para que el ViewModel la maneje. */
    suspend fun getAlbums(): List<Album> = service.getAlbums()

    companion object {
        fun create(context: Context): AlbumRepository =
            AlbumRepository(NetworkAlbumService(NetworkServiceAdapter.getInstance(context)))
    }
}
