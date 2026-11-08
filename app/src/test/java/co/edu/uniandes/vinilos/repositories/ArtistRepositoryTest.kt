package co.edu.uniandes.vinilos.repositories

import co.edu.uniandes.vinilos.fakes.FakeArtistService
import co.edu.uniandes.vinilos.fakes.FakeArtistService.Companion.artist
import co.edu.uniandes.vinilos.models.ArtistType.BAND
import co.edu.uniandes.vinilos.models.ArtistType.MUSICIAN
import com.android.volley.VolleyError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistRepositoryTest {

    @Test
    fun getArtists_joinsMusiciansAndBandsSortedByName() = runTest {
        val service = FakeArtistService(
            musicians = { listOf(artist(100, "Rubén Blades", MUSICIAN), artist(102, "Carlos Vives", MUSICIAN)) },
            bands = { listOf(artist(101, "Queen", BAND), artist(103, "Aterciopelados", BAND)) }
        )

        val artists = ArtistRepository(service).getArtists()

        assertEquals(listOf("Aterciopelados", "Carlos Vives", "Queen", "Rubén Blades"), artists.map { it.name })
        assertEquals(listOf(BAND, MUSICIAN, BAND, MUSICIAN), artists.map { it.type })
    }

    @Test
    fun getArtists_withNoArtists_returnsEmptyList() = runTest {
        assertTrue(ArtistRepository(FakeArtistService()).getArtists().isEmpty())
    }

    @Test
    fun getArtists_requestsMusiciansAndBandsInParallel() = runTest {
        // Ninguna respuesta llega hasta abrir la compuerta: si fueran en secuencia, solo empezaría una.
        val gate = CompletableDeferred<Unit>()
        val service = FakeArtistService(
            musicians = { gate.await(); listOf(artist(1, "Ana", MUSICIAN)) },
            bands = { gate.await(); listOf(artist(2, "Banda", BAND)) }
        )

        val result = async { ArtistRepository(service).getArtists() }
        runCurrent()

        assertEquals(1, service.musicianCalls)
        assertEquals(1, service.bandCalls)
        gate.complete(Unit)
        assertEquals(2, result.await().size)
    }

    @Test(expected = VolleyError::class)
    fun getArtists_whenBandsFail_throws() = runTest {
        val service = FakeArtistService(
            musicians = { listOf(artist(1, "Ana", MUSICIAN)) },
            bands = { throw VolleyError("sin conexión") }
        )

        ArtistRepository(service).getArtists()
    }

    @Test
    fun sortByName_ignoresCaseAndAccents() {
        val artists = listOf(
            artist(1, "zoe", MUSICIAN),
            artist(2, "Ángela Aguilar", MUSICIAN),
            artist(3, "Bomba Estéreo", BAND),
            artist(4, "andrés Cepeda", MUSICIAN)
        )

        val names = ArtistRepository.sortByName(artists).map { it.name }

        assertEquals(listOf("andrés Cepeda", "Ángela Aguilar", "Bomba Estéreo", "zoe"), names)
    }
}
