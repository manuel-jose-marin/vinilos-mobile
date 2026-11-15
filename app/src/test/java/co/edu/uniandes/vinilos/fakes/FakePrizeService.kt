package co.edu.uniandes.vinilos.fakes

import co.edu.uniandes.vinilos.models.Prize
import co.edu.uniandes.vinilos.network.PrizeService

/** Servicio de premios para pruebas: devuelve lo que indique [result] (por defecto, ninguno) y cuenta las llamadas. */
class FakePrizeService(var result: suspend () -> List<Prize> = { emptyList() }) : PrizeService {
    var calls = 0
        private set

    override suspend fun getPrizes(): List<Prize> {
        calls++
        return result()
    }
}
