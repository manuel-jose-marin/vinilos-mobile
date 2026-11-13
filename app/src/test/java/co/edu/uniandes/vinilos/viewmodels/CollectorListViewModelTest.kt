package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.repositories.CollectorRepository
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
class CollectorListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun collector(id: Int) = Collector(id, "Coleccionista $id", "3001234567", "col$id@test.com", emptyList(), 1)

    private fun viewModel(service: FakeCollectorService) = CollectorListViewModel(CollectorRepository(service))

    @Test
    fun init_showsLoadingUntilTheRepositoryResponds() = runTest(dispatcher) {
        val vm = viewModel(FakeCollectorService { listOf(collector(1)) })

        assertEquals(CollectorListState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is CollectorListState.Content)
    }

    @Test
    fun loadCollectors_withCollectors_exposesContent() = runTest(dispatcher) {
        val vm = viewModel(FakeCollectorService { listOf(collector(1), collector(2)) })
        advanceUntilIdle()

        val state = vm.state.value as CollectorListState.Content
        assertEquals(listOf(1, 2), state.collectors.map { it.id })
    }

    @Test
    fun loadCollectors_withNoCollectors_exposesEmpty() = runTest(dispatcher) {
        val vm = viewModel(FakeCollectorService { emptyList() })
        advanceUntilIdle()

        assertEquals(CollectorListState.Empty, vm.state.value)
    }

    @Test
    fun loadCollectors_whenTheRequestFails_exposesError() = runTest(dispatcher) {
        val vm = viewModel(FakeCollectorService { throw VolleyError("sin conexión") })
        advanceUntilIdle()

        assertEquals(CollectorListState.Error, vm.state.value)
    }

    @Test
    fun retry_afterAnError_loadsTheCollectorsAgain() = runTest(dispatcher) {
        val service = FakeCollectorService { throw VolleyError("sin conexión") }
        val vm = viewModel(service)
        advanceUntilIdle()
        assertEquals(CollectorListState.Error, vm.state.value)

        service.result = { listOf(collector(1)) }
        vm.loadCollectors()
        assertEquals(CollectorListState.Loading, vm.state.value)
        advanceUntilIdle()

        assertTrue(vm.state.value is CollectorListState.Content)
    }
}
