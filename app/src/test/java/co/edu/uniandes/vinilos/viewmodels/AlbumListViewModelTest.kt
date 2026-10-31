package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.network.AlbumService
import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.repositories.AlbumRepository
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
class AlbumListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeAlbumService(var result: () -> List<Album>) : AlbumService {
        var calls = 0
        override suspend fun getAlbums(): List<Album> {
            calls++
            return result()
        }

        override suspend fun getAlbum(id: Int): Album = error("No se usa en el listado")
    }

    private fun album(id: Int) = Album(id, "Álbum $id", "", "1984-08-01T00:00:00.000Z", "", "Salsa", "Elektra", emptyList(), emptyList(), emptyList())

    private fun viewModel(service: FakeAlbumService) = AlbumListViewModel(AlbumRepository(service, FakeCollectorService()))

    @Test
    fun init_showsLoadingUntilTheRepositoryResponds() = runTest(dispatcher) {
        val vm = viewModel(FakeAlbumService { listOf(album(1)) })

        assertEquals(AlbumListState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is AlbumListState.Content)
    }

    @Test
    fun loadAlbums_withAlbums_exposesContent() = runTest(dispatcher) {
        val vm = viewModel(FakeAlbumService { listOf(album(1), album(2)) })
        advanceUntilIdle()

        val state = vm.state.value as AlbumListState.Content
        assertEquals(listOf(1, 2), state.albums.map { it.id })
    }

    @Test
    fun loadAlbums_withNoAlbums_exposesEmpty() = runTest(dispatcher) {
        val vm = viewModel(FakeAlbumService { emptyList() })
        advanceUntilIdle()

        assertEquals(AlbumListState.Empty, vm.state.value)
    }

    @Test
    fun loadAlbums_whenTheRequestFails_exposesError() = runTest(dispatcher) {
        val vm = viewModel(FakeAlbumService { throw VolleyError("sin conexión") })
        advanceUntilIdle()

        assertEquals(AlbumListState.Error, vm.state.value)
    }

    @Test
    fun retry_afterAnError_loadsTheAlbumsAgain() = runTest(dispatcher) {
        val service = FakeAlbumService { throw VolleyError("sin conexión") }
        val vm = viewModel(service)
        advanceUntilIdle()
        assertEquals(AlbumListState.Error, vm.state.value)

        service.result = { listOf(album(1)) }
        vm.loadAlbums()
        assertEquals(AlbumListState.Loading, vm.state.value)
        advanceUntilIdle()

        assertTrue(vm.state.value is AlbumListState.Content)
        assertEquals(2, service.calls)
    }
}
