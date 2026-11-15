package co.edu.uniandes.vinilos.repositories

import android.content.Context
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.network.CollectorService
import co.edu.uniandes.vinilos.network.NetworkCollectorService
import co.edu.uniandes.vinilos.network.NetworkServiceAdapter

/** Fuente de datos de coleccionistas para los ViewModels; oculta de dónde vienen (hoy, la API REST). */
class CollectorRepository(private val service: CollectorService) {

    /** Listado de coleccionistas. Si falla la red o el servidor, lanza la excepción para que el ViewModel la maneje. */
    suspend fun getCollectors(): List<Collector> = service.getCollectors()

    companion object {
        fun create(context: Context): CollectorRepository {
            val network = NetworkServiceAdapter.getInstance(context)
            return CollectorRepository(NetworkCollectorService(network))
        }
    }
}
