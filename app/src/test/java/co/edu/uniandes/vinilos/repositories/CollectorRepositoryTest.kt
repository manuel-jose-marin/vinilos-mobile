package co.edu.uniandes.vinilos.repositories

import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.models.Collector
import com.android.volley.VolleyError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectorRepositoryTest {

    private val manolo = Collector(100, "Manolo Bellon", "3001234567", "manolo@bellon.com", emptyList(), 2)

    @Test
    fun getCollectors_returnsTheListFromService() = runTest {
        val repository = CollectorRepository(FakeCollectorService { listOf(manolo) })

        val collectors = repository.getCollectors()

        assertEquals(listOf(manolo), collectors)
    }

    @Test(expected = VolleyError::class)
    fun getCollectors_propagatesExceptionsFromTheService() = runTest {
        val repository = CollectorRepository(FakeCollectorService { throw VolleyError("Error de red") })

        repository.getCollectors()
    }
}
