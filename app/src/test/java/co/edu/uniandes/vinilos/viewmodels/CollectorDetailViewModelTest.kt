package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.fakes.FakeAlbumService
import co.edu.uniandes.vinilos.fakes.FakeAlbumService.Companion.album
import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.models.CollectorAlbum
import co.edu.uniandes.vinilos.repositories.CollectorRepository
import com.android.volley.VolleyError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectorDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val manolo = Collector(100, "Manolo Bellon", "3502457896", "manollo@caracol.com.co", emptyList(), 1)

    private val buscandoAmerica = CollectorAlbum(100, 35, CollectorAlbum.STATUS_ACTIVE, album(100, "Buscando América"))

    private fun service(collector: suspend (Int) -> Collector = { manolo }) =
        FakeCollectorService(collector = collector, collectorAlbums = { listOf(buscandoAmerica) })

    private fun viewModel(id: Int, service: FakeCollectorService) =
        CollectorDetailViewModel(id, CollectorRepository(service, FakeAlbumService()))

    private val CollectorDetailViewModel.content get() = state.value as CollectorDetailState.Content

    @Test
    fun init_showsLoadingUntilTheRepositoryResponds() = runTest(dispatcher) {
        val vm = viewModel(100, service())

        assertEquals(CollectorDetailState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is CollectorDetailState.Content)
    }

    @Test
    fun loadCollector_requestsTheGivenCollectorAndExposesItsDetail() = runTest(dispatcher) {
        val service = service()
        val vm = viewModel(100, service)
        advanceUntilIdle()

        assertEquals(listOf(100), service.detailRequests)
        assertEquals(manolo, vm.content.detail.collector)
        assertEquals(listOf(buscandoAmerica), vm.content.detail.albums)
    }

    @Test
    fun loadCollector_withoutFavoritesAlbumsOrComments_exposesContentWithEmptyLists() = runTest(dispatcher) {
        val service = FakeCollectorService(collector = { manolo.copy(albumCount = 0) })
        val vm = viewModel(100, service)
        advanceUntilIdle()

        val detail = vm.content.detail
        assertTrue(detail.collector.favoritePerformers.isEmpty())
        assertTrue(detail.collector.comments.isEmpty())
        assertTrue(detail.albums.isEmpty())
    }

    @Test
    fun loadCollector_whenTheRequestFails_exposesError() = runTest(dispatcher) {
        val vm = viewModel(100, service(collector = { throw VolleyError("sin conexión") }))
        advanceUntilIdle()

        assertEquals(CollectorDetailState.Error, vm.state.value)
    }

    @Test
    fun retry_afterAnError_loadsTheCollectorAgain() = runTest(dispatcher) {
        var fail = true
        val service = service(collector = { if (fail) throw VolleyError("sin conexión") else manolo })
        val vm = viewModel(100, service)
        advanceUntilIdle()
        assertEquals(CollectorDetailState.Error, vm.state.value)

        fail = false
        vm.loadCollector()
        assertEquals(CollectorDetailState.Loading, vm.state.value)
        advanceUntilIdle()

        assertEquals(manolo, vm.content.detail.collector)
        assertEquals(listOf(100, 100), service.detailRequests)
    }

    /** Si el usuario reintenta mientras carga, la respuesta tardía de la primera petición no reemplaza a la nueva. */
    @Test
    fun retry_whileLoading_cancelsThePreviousRequest() = runTest(dispatcher) {
        val slowAnswer = CompletableDeferred<Collector>()
        val service = service(collector = { slowAnswer.await() })
        val vm = viewModel(100, service)
        runCurrent()

        service.collector = { manolo }
        vm.loadCollector()
        advanceUntilIdle()
        slowAnswer.complete(manolo.copy(name = "Respuesta vieja"))
        advanceUntilIdle()

        assertEquals("Manolo Bellon", vm.content.detail.collector.name)
    }
}
