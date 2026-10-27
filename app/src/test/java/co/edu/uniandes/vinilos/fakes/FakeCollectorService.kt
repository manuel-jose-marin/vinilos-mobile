package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.network.CollectorService

/** Servicio de coleccionistas para pruebas: devuelve lo que indique [result] (por defecto, ninguno). */
class FakeCollectorService(var result: () -> List<Collector> = { emptyList() }) : CollectorService {
    override suspend fun getCollectors(): List<Collector> = result()
}
