package co.edu.uniandes.vinilos.repositories

import android.content.Context
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.network.AlbumService
import co.edu.uniandes.vinilos.network.CollectorService
import co.edu.uniandes.vinilos.network.NetworkAlbumService
import co.edu.uniandes.vinilos.network.NetworkCollectorService
import co.edu.uniandes.vinilos.network.NetworkServiceAdapter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Fuente de datos de álbumes para los ViewModels; oculta de dónde vienen (hoy, la API REST). */
class AlbumRepository(
    private val service: AlbumService,
    private val collectorService: CollectorService
) {

    /** Catálogo de álbumes. Si falla la red o el servidor, lanza la excepción para que el ViewModel la maneje. */
    suspend fun getAlbums(): List<Album> = service.getAlbums()

    /**
     * Un álbum con sus artistas, tracks y comentarios, con el autor de cada comentario.
     * Si falla la red, el servidor o el álbum no existe, lanza la excepción. Si solo fallan los
     * coleccionistas, devuelve el álbum con los comentarios sin autor.
     */
    suspend fun getAlbum(id: Int): Album = coroutineScope {
        val collectors = async { getCollectorsOrEmpty() }
        val album = service.getAlbum(id)
        val authors = commentAuthors(collectors.await())
        album.copy(comments = album.comments.map { it.copy(author = authors[it.id]) })
    }

    private suspend fun getCollectorsOrEmpty(): List<Collector> = try {
        collectorService.getCollectors()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        fun create(context: Context): AlbumRepository {
            val network = NetworkServiceAdapter.getInstance(context)
            return AlbumRepository(NetworkAlbumService(network), NetworkCollectorService(network))
        }

        /** Nombre del coleccionista que escribió cada comentario, por id de comentario. */
        fun commentAuthors(collectors: List<Collector>): Map<Int, String> =
            collectors.flatMap { collector -> collector.comments.map { it.id to collector.name } }.toMap()
    }
}
