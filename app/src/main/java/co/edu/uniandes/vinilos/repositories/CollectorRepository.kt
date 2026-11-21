package co.edu.uniandes.vinilos.repositories

import android.content.Context
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.models.CollectorDetail
import co.edu.uniandes.vinilos.models.Performer
import co.edu.uniandes.vinilos.network.AlbumService
import co.edu.uniandes.vinilos.network.CollectorService
import co.edu.uniandes.vinilos.network.NetworkAlbumService
import co.edu.uniandes.vinilos.network.NetworkCollectorService
import co.edu.uniandes.vinilos.network.NetworkServiceAdapter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Fuente de datos de coleccionistas para los ViewModels; oculta de dónde vienen (hoy, la API REST). */
class CollectorRepository(
    private val service: CollectorService,
    private val albumService: AlbumService
) {

    /** Listado de coleccionistas. Si falla la red o el servidor, lanza la excepción para que el ViewModel la maneje. */
    suspend fun getCollectors(): List<Collector> = service.getCollectors()

    /**
     * Detalle de un coleccionista: sus datos, favoritos, comentarios y colección, con el artista de cada álbum.
     * Las tres peticiones van en paralelo. Si falla el coleccionista o su colección, lanza la excepción. Si solo
     * falla el catálogo de álbumes, devuelve la colección sin artistas y la vista muestra "Artista desconocido".
     */
    suspend fun getCollectorDetail(id: Int): CollectorDetail = coroutineScope {
        val catalog = async { getAlbumsOrEmpty() }
        val collectorAlbums = async { service.getCollectorAlbums(id) }
        val collector = service.getCollector(id)
        val performers = albumPerformers(catalog.await())
        val albums = collectorAlbums.await().map {
            it.copy(album = it.album.copy(performers = performers[it.album.id].orEmpty()))
        }
        CollectorDetail(collector, albums)
    }

    private suspend fun getAlbumsOrEmpty(): List<Album> = try {
        albumService.getAlbums()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        fun create(context: Context): CollectorRepository {
            val network = NetworkServiceAdapter.getInstance(context)
            return CollectorRepository(NetworkCollectorService(network), NetworkAlbumService(network))
        }

        /** Artistas de cada álbum del catálogo, por id de álbum. */
        fun albumPerformers(catalog: List<Album>): Map<Int, List<Performer>> =
            catalog.associate { it.id to it.performers }
    }
}
