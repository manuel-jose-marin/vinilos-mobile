package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Comment
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.network.AlbumService
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
class AlbumDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeAlbumService(var result: (Int) -> Album) : AlbumService {
        val requestedIds = mutableListOf<Int>()

        override suspend fun getAlbums(): List<Album> = error("No se usa en el detalle")

        override suspend fun getAlbum(id: Int): Album {
            requestedIds += id
            return result(id)
        }
    }

    private fun album(id: Int, tracks: List<Track> = emptyList(), comments: List<Comment> = emptyList()) =
        Album(id, "Álbum $id", "", "1984-08-01T00:00:00.000Z", "Descripción", "Salsa", "Elektra", emptyList(), tracks, comments)

    private fun viewModel(albumId: Int, service: FakeAlbumService) =
        AlbumDetailViewModel(albumId, AlbumRepository(service))

    @Test
    fun init_showsLoadingUntilTheRepositoryResponds() = runTest(dispatcher) {
        val vm = viewModel(100, FakeAlbumService { album(it) })

        assertEquals(AlbumDetailState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is AlbumDetailState.Content)
    }

    @Test
    fun loadAlbum_requestsTheGivenAlbumAndExposesIt() = runTest(dispatcher) {
        val tracks = listOf(Track(1, "Decisiones", "5:05"))
        val comments = listOf(Comment(1, "Excelente", 5))
        val service = FakeAlbumService { album(it, tracks, comments) }
        val vm = viewModel(100, service)
        advanceUntilIdle()

        val state = vm.state.value as AlbumDetailState.Content
        assertEquals(listOf(100), service.requestedIds)
        assertEquals(100, state.album.id)
        assertEquals(tracks, state.album.tracks)
        assertEquals(comments, state.album.comments)
    }

    @Test
    fun loadAlbum_withoutTracksOrComments_exposesContentWithEmptyLists() = runTest(dispatcher) {
        val vm = viewModel(101, FakeAlbumService { album(it) })
        advanceUntilIdle()

        val album = (vm.state.value as AlbumDetailState.Content).album
        assertTrue(album.tracks.isEmpty())
        assertTrue(album.comments.isEmpty())
    }

    @Test
    fun loadAlbum_whenTheRequestFails_exposesError() = runTest(dispatcher) {
        val vm = viewModel(100, FakeAlbumService { throw VolleyError("sin conexión") })
        advanceUntilIdle()

        assertEquals(AlbumDetailState.Error, vm.state.value)
    }

    @Test
    fun retry_afterAnError_loadsTheAlbumAgain() = runTest(dispatcher) {
        val service = FakeAlbumService { throw VolleyError("sin conexión") }
        val vm = viewModel(100, service)
        advanceUntilIdle()
        assertEquals(AlbumDetailState.Error, vm.state.value)

        service.result = { album(it) }
        vm.loadAlbum()
        assertEquals(AlbumDetailState.Loading, vm.state.value)
        advanceUntilIdle()

        assertTrue(vm.state.value is AlbumDetailState.Content)
        assertEquals(listOf(100, 100), service.requestedIds)
    }
}
