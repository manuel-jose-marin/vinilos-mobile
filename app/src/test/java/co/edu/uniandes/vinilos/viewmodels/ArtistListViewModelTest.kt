package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.fakes.FakeArtistService
import co.edu.uniandes.vinilos.fakes.FakeArtistService.Companion.artist
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
class ArtistListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val ruben = artist(100, "Rubén Blades", MUSICIAN)
    private val queen = artist(101, "Queen", BAND)

    private fun service() = FakeArtistService(musicians = { listOf(ruben) }, bands = { listOf(queen) })

    private fun viewModel(service: FakeArtistService) = ArtistListViewModel(ArtistRepository(service))

    private fun ArtistListViewModel.content() = state.value as ArtistListState.Content

    @Test
    fun init_showsLoadingUntilTheRepositoryResponds() = runTest(dispatcher) {
        val vm = viewModel(service())

        assertEquals(ArtistListState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is ArtistListState.Content)
    }

    @Test
    fun loadArtists_showsMusiciansAndBandsSortedWithAllFilter() = runTest(dispatcher) {
        val vm = viewModel(service())
        advanceUntilIdle()

        assertEquals(listOf("Queen", "Rubén Blades"), vm.content().artists.map { it.name })
        assertEquals(ArtistFilter.ALL, vm.content().filter)
    }

    @Test
    fun loadArtists_empty_whenTheServerHasNoArtists() = runTest(dispatcher) {
        val vm = viewModel(FakeArtistService())
        advanceUntilIdle()

        assertEquals(ArtistListState.Empty, vm.state.value)
    }

    @Test
    fun loadArtists_error_whenARequestFails() = runTest(dispatcher) {
        val vm = viewModel(FakeArtistService(bands = { throw VolleyError("sin conexión") }))
        advanceUntilIdle()

        assertEquals(ArtistListState.Error, vm.state.value)
    }

    @Test
    fun retry_loadsAgainAfterAnError() = runTest(dispatcher) {
        val service = FakeArtistService(musicians = { throw VolleyError("sin conexión") })
        val vm = viewModel(service)
        advanceUntilIdle()
        assertEquals(ArtistListState.Error, vm.state.value)

        service.musicians = { listOf(ruben) }
        vm.loadArtists()
        assertEquals(ArtistListState.Loading, vm.state.value)
        advanceUntilIdle()

        assertEquals(listOf(ruben), vm.content().artists)
        assertEquals(2, service.musicianCalls)
    }

    @Test
    fun setFilter_filtersInMemory() = runTest(dispatcher) {
        val service = service()
        val vm = viewModel(service)
        advanceUntilIdle()

        vm.setFilter(ArtistFilter.MUSICIANS)
        assertEquals(listOf(ruben), vm.content().artists)
        assertEquals(ArtistFilter.MUSICIANS, vm.content().filter)

        vm.setFilter(ArtistFilter.BANDS)
        assertEquals(listOf(queen), vm.content().artists)
        assertEquals(ArtistFilter.BANDS, vm.content().filter)

        vm.setFilter(ArtistFilter.ALL)
        assertEquals(listOf(queen, ruben), vm.content().artists)

        // Cambiar de filtro no vuelve a pedir los artistas.
        assertEquals(1, service.musicianCalls)
        assertEquals(1, service.bandCalls)
    }

    @Test
    fun setFilter_withNoArtistsOfThatType_showsEmptyContentNotEmptyState() = runTest(dispatcher) {
        val vm = viewModel(FakeArtistService(musicians = { listOf(ruben) }))
        advanceUntilIdle()

        vm.setFilter(ArtistFilter.BANDS)

        assertTrue(vm.content().artists.isEmpty())
        assertEquals(ArtistFilter.BANDS, vm.content().filter)
    }

    @Test
    fun setFilter_whileLoading_isAppliedWhenTheArtistsArrive() = runTest(dispatcher) {
        val vm = viewModel(service())

        vm.setFilter(ArtistFilter.BANDS)
        assertEquals(ArtistListState.Loading, vm.state.value)
        advanceUntilIdle()

        assertEquals(listOf(queen), vm.content().artists)
        assertEquals(ArtistFilter.BANDS, vm.content().filter)
    }

    @Test
    fun retry_keepsTheSelectedFilter() = runTest(dispatcher) {
        val vm = viewModel(service())
        advanceUntilIdle()
        vm.setFilter(ArtistFilter.MUSICIANS)

        vm.loadArtists()
        advanceUntilIdle()

        assertEquals(ArtistFilter.MUSICIANS, vm.content().filter)
        assertEquals(listOf(ruben), vm.content().artists)
    }
}
