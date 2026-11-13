package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.fakes.FakeArtistService
import co.edu.uniandes.vinilos.fakes.FakeArtistService.Companion.artist
import co.edu.uniandes.vinilos.fakes.FakePrizeService
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistType
import co.edu.uniandes.vinilos.models.ArtistType.BAND
import co.edu.uniandes.vinilos.models.ArtistType.MUSICIAN
import co.edu.uniandes.vinilos.repositories.ArtistRepository
import com.android.volley.VolleyError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val ruben = artist(100, "Rubén Blades", MUSICIAN)
    private val queen = artist(101, "Queen", BAND)

    private fun service(
        musicians: suspend () -> List<Artist> = { listOf(ruben) },
        bands: suspend () -> List<Artist> = { listOf(queen) }
    ) = FakeArtistService(musicians, bands)

    private fun viewModel(id: Int, type: ArtistType, service: FakeArtistService) =
        ArtistDetailViewModel(id, type, ArtistRepository(service, FakePrizeService()))

    @Test
    fun init_showsLoadingUntilTheRepositoryResponds() = runTest(dispatcher) {
        val vm = viewModel(100, MUSICIAN, service())

        assertEquals(ArtistDetailState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is ArtistDetailState.Content)
    }

    @Test
    fun loadArtist_musician_requestsTheGivenMusicianAndExposesIt() = runTest(dispatcher) {
        val service = service()
        val vm = viewModel(100, MUSICIAN, service)
        advanceUntilIdle()

        assertEquals(listOf(MUSICIAN to 100), service.detailRequests)
        assertEquals(ruben, (vm.state.value as ArtistDetailState.Content).artist)
    }

    @Test
    fun loadArtist_band_requestsTheGivenBandAndExposesIt() = runTest(dispatcher) {
        val service = service()
        val vm = viewModel(101, BAND, service)
        advanceUntilIdle()

        assertEquals(listOf(BAND to 101), service.detailRequests)
        assertEquals(queen, (vm.state.value as ArtistDetailState.Content).artist)
    }

    @Test
    fun loadArtist_withoutAlbumsPrizesOrMembers_exposesContentWithEmptyLists() = runTest(dispatcher) {
        val vm = viewModel(100, MUSICIAN, service())
        advanceUntilIdle()

        val artist = (vm.state.value as ArtistDetailState.Content).artist
        assertTrue(artist.albums.isEmpty())
        assertTrue(artist.prizes.isEmpty())
        assertTrue(artist.members.isEmpty())
    }

    @Test
    fun loadArtist_whenTheRequestFails_exposesError() = runTest(dispatcher) {
        val vm = viewModel(100, MUSICIAN, service(musicians = { throw VolleyError("sin conexión") }))
        advanceUntilIdle()

        assertEquals(ArtistDetailState.Error, vm.state.value)
    }

    @Test
    fun retry_afterAnError_loadsTheArtistAgain() = runTest(dispatcher) {
        var fail = true
        val service = service(musicians = { if (fail) throw VolleyError("sin conexión") else listOf(ruben) })
        val vm = viewModel(100, MUSICIAN, service)
        advanceUntilIdle()
        assertEquals(ArtistDetailState.Error, vm.state.value)

        fail = false
        vm.loadArtist()
        assertEquals(ArtistDetailState.Loading, vm.state.value)
        advanceUntilIdle()

        assertTrue(vm.state.value is ArtistDetailState.Content)
        assertEquals(listOf(MUSICIAN to 100, MUSICIAN to 100), service.detailRequests)
    }
}
