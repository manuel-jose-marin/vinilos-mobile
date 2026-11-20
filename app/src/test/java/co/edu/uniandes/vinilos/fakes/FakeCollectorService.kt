package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.models.CollectorAlbum
import co.edu.uniandes.vinilos.network.CollectorService

/**
 * Servicio de coleccionistas para pruebas: devuelve lo que indique [result] (por defecto, ninguno) en el listado, y
 * [collector] y [collectorAlbums] en el detalle. Registra en [detailRequests] el id de cada detalle pedido.
 */
class FakeCollectorService(
    var collector: suspend (Int) -> Collector = { error("Detalle del coleccionista $it no configurado") },
    var collectorAlbums: suspend (Int) -> List<CollectorAlbum> = { emptyList() },
    var result: suspend () -> List<Collector> = { emptyList() }
) : CollectorService {

    val detailRequests = mutableListOf<Int>()

    override suspend fun getCollectors(): List<Collector> = result()

    override suspend fun getCollector(id: Int): Collector {
        detailRequests += id
        return collector(id)
    }

    override suspend fun getCollectorAlbums(id: Int): List<CollectorAlbum> = collectorAlbums(id)
}
