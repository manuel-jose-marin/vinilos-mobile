package co.edu.uniandes.vinilos.repositories

import co.edu.uniandes.vinilos.fakes.FakeArtistService
import co.edu.uniandes.vinilos.fakes.FakeArtistService.Companion.artist
import co.edu.uniandes.vinilos.fakes.FakePrizeService
import co.edu.uniandes.vinilos.models.ArtistPrize
import co.edu.uniandes.vinilos.models.ArtistType.BAND
import co.edu.uniandes.vinilos.models.ArtistType.MUSICIAN
import co.edu.uniandes.vinilos.models.Prize
import com.android.volley.VolleyError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        val artists = ArtistRepository(service, FakePrizeService()).getArtists()

        assertEquals(listOf("Aterciopelados", "Carlos Vives", "Queen", "Rubén Blades"), artists.map { it.name })
        assertEquals(listOf(BAND, MUSICIAN, BAND, MUSICIAN), artists.map { it.type })
    }

    @Test
    fun getArtists_withNoArtists_returnsEmptyList() = runTest {
        assertTrue(ArtistRepository(FakeArtistService(), FakePrizeService()).getArtists().isEmpty())
    }

    @Test
    fun getArtists_requestsMusiciansAndBandsInParallel() = runTest {
        // Ninguna respuesta llega hasta abrir la compuerta: si fueran en secuencia, solo empezaría una.
        val gate = CompletableDeferred<Unit>()
        val service = FakeArtistService(
            musicians = { gate.await(); listOf(artist(1, "Ana", MUSICIAN)) },
            bands = { gate.await(); listOf(artist(2, "Banda", BAND)) }
        )

        val result = async { ArtistRepository(service, FakePrizeService()).getArtists() }
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

        ArtistRepository(service, FakePrizeService()).getArtists()
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

    private val nobel = Prize(1, "Premio Nobel", "Academia Sueca", performerPrizeIds = listOf(10))
    private val grammy = Prize(2, "Grammy", "Recording Academy", performerPrizeIds = listOf(11, 12))

    private val rubenWithPrizes = artist(100, "Rubén Blades", MUSICIAN).copy(
        prizes = listOf(ArtistPrize(10, "1978-12-10T00:00:00.000Z"), ArtistPrize(12, "1986-02-25T00:00:00.000Z"))
    )
    private val queenWithPrizes = artist(101, "Queen", BAND).copy(prizes = listOf(ArtistPrize(11, "1990-01-01T00:00:00.000Z")))

    @Test
    fun getArtist_musician_requestsMusicianEndpoint() = runTest {
        val service = FakeArtistService(musicians = { listOf(rubenWithPrizes) })

        val artist = ArtistRepository(service, FakePrizeService()).getArtist(100, MUSICIAN)

        assertEquals(listOf(MUSICIAN to 100), service.detailRequests)
        assertEquals("Rubén Blades", artist.name)
    }

    @Test
    fun getArtist_band_requestsBandEndpoint() = runTest {
        val service = FakeArtistService(bands = { listOf(queenWithPrizes) })

        val artist = ArtistRepository(service, FakePrizeService()).getArtist(101, BAND)

        assertEquals(listOf(BAND to 101), service.detailRequests)
        assertEquals("Queen", artist.name)
    }

    @Test
    fun getArtist_completesPrizeNameAndOrganization() = runTest {
        val service = FakeArtistService(musicians = { listOf(rubenWithPrizes) })
        val prizes = FakePrizeService { listOf(nobel, grammy) }

        val artist = ArtistRepository(service, prizes).getArtist(100, MUSICIAN)

        assertEquals(
            listOf(
                ArtistPrize(10, "1978-12-10T00:00:00.000Z", "Premio Nobel", "Academia Sueca"),
                ArtistPrize(12, "1986-02-25T00:00:00.000Z", "Grammy", "Recording Academy")
            ),
            artist.prizes
        )
    }

    @Test
    fun getArtist_whenPrizesFail_returnsArtistWithPrizesWithoutName() = runTest {
        val service = FakeArtistService(musicians = { listOf(rubenWithPrizes) })
        val prizes = FakePrizeService { throw VolleyError("sin conexión") }

        val artist = ArtistRepository(service, prizes).getArtist(100, MUSICIAN)

        assertEquals(listOf(10, 12), artist.prizes.map { it.id })
        assertTrue(artist.prizes.all { it.name == null && it.organization == null })
    }

    @Test
    fun getArtist_requestsArtistAndPrizesInParallel() = runTest {
        val gate = CompletableDeferred<Unit>()
        val service = FakeArtistService(musicians = { gate.await(); listOf(rubenWithPrizes) })
        val prizes = FakePrizeService { gate.await(); listOf(nobel) }

        val result = async { ArtistRepository(service, prizes).getArtist(100, MUSICIAN) }
        runCurrent()

        assertEquals(1, service.detailRequests.size)
        assertEquals(1, prizes.calls)
        gate.complete(Unit)
        assertEquals("Premio Nobel", result.await().prizes.first().name)
    }

    @Test(expected = VolleyError::class)
    fun getArtist_whenArtistFails_throws() = runTest {
        val service = FakeArtistService(bands = { throw VolleyError("sin conexión") })

        ArtistRepository(service, FakePrizeService { listOf(nobel) }).getArtist(101, BAND)
    }

    @Test
    fun withPrizeNames_leavesUnknownPrizeWithoutName() {
        val artist = artist(1, "Ana", MUSICIAN).copy(prizes = listOf(ArtistPrize(99, "2000-01-01T00:00:00.000Z")))

        val prize = ArtistRepository.withPrizeNames(artist, listOf(nobel)).prizes.single()

        assertNull(prize.name)
        assertNull(prize.organization)
    }
}
