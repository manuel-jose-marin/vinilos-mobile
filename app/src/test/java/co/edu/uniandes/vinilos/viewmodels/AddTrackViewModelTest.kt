package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.fakes.FakeAlbumService
import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.models.NewTrack
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.validation.TrackField
import co.edu.uniandes.vinilos.validation.TrackForm
import co.edu.uniandes.vinilos.validation.TrackFormError
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
class AddTrackViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val service = FakeAlbumService()

    private val validForm = TrackForm(name = "  Plástico ", duration = " 6:52 ")

    private val expectedRequest = NewTrack(name = "Plástico", duration = "6:52")

    private fun viewModel(albumId: Int = 100) =
        AddTrackViewModel(albumId, AlbumRepository(service, FakeCollectorService()))

    @Test
    fun initialState_isEditingWithoutErrors() {
        assertEquals(AddTrackState.Editing(), viewModel().state.value)
    }

    /** Criterios 3, 5 y 6 de HU08: los errores se muestran por campo y el track no se envía. */
    @Test
    fun save_withAnInvalidForm_showsTheErrorOfEachFieldAndSendsNothing() = runTest(dispatcher) {
        val vm = viewModel()

        vm.save(TrackForm(name = " ", duration = "6.31"))
        advanceUntilIdle()

        val expected = mapOf(
            TrackField.NAME to TrackFormError.NAME_REQUIRED,
            TrackField.DURATION to TrackFormError.DURATION_FORMAT
        )
        assertEquals(AddTrackState.Editing(expected), vm.state.value)
        assertTrue(service.trackRequests.isEmpty())
    }

    @Test
    fun save_withSecondsOver59_showsTheSecondsErrorAndSendsNothing() = runTest(dispatcher) {
        val vm = viewModel()

        vm.save(validForm.copy(duration = "06:75"))
        advanceUntilIdle()

        assertEquals(AddTrackState.Editing(mapOf(TrackField.DURATION to TrackFormError.DURATION_SECONDS)), vm.state.value)
        assertTrue(service.trackRequests.isEmpty())
    }

    /** Criterios 7 y 8 de HU08: "Agregar" queda deshabilitado mientras se guarda y luego llega el track creado. */
    @Test
    fun save_withAValidForm_showsSavingAndThenTheAddedTrack() = runTest(dispatcher) {
        service.addedTrack = { _, track -> Track(250, track.name, track.duration) }
        val vm = viewModel(albumId = 100)

        vm.save(validForm)
        assertEquals(AddTrackState.Saving, vm.state.value)
        advanceUntilIdle()

        assertEquals(AddTrackState.Added(Track(250, "Plástico", "6:52")), vm.state.value)
        assertEquals(listOf(100 to expectedRequest), service.trackRequests)
    }

    @Test
    fun save_whenTheFormWasCorrected_sendsItAfterTheErrors() = runTest(dispatcher) {
        val vm = viewModel()
        vm.save(validForm.copy(duration = ""))
        assertTrue(vm.state.value is AddTrackState.Editing)

        vm.save(validForm)
        advanceUntilIdle()

        assertTrue(vm.state.value is AddTrackState.Added)
        assertEquals(listOf(100 to expectedRequest), service.trackRequests)
    }

    @Test
    fun save_whileSaving_doesNotSendTheTrackTwice() = runTest(dispatcher) {
        val answer = CompletableDeferred<Track>()
        service.addedTrack = { _, _ -> answer.await() }
        val vm = viewModel()

        vm.save(validForm)
        runCurrent()
        vm.save(validForm)
        vm.save(validForm.copy(name = "Otro"))
        runCurrent()
        assertEquals(AddTrackState.Saving, vm.state.value)

        answer.complete(Track(250, "Plástico", "6:52"))
        advanceUntilIdle()

        assertEquals(1, service.trackRequests.size)
        assertTrue(vm.state.value is AddTrackState.Added)
    }

    @Test
    fun save_afterTheTrackWasAdded_doesNothing() = runTest(dispatcher) {
        val vm = viewModel()
        vm.save(validForm)
        advanceUntilIdle()
        val added = vm.state.value

        vm.save(validForm)
        advanceUntilIdle()

        assertEquals(added, vm.state.value)
        assertEquals(1, service.trackRequests.size)
    }

    /** Criterio 9 de HU08: con un error del servidor la hoja sigue abierta y se puede reintentar con los mismos datos. */
    @Test
    fun save_whenTheServerFails_showsSaveErrorAndAllowsRetrying() = runTest(dispatcher) {
        var fail = true
        service.addedTrack = { _, track -> if (fail) throw VolleyError("sin conexión") else Track(250, track.name, track.duration) }
        val vm = viewModel()

        vm.save(validForm)
        advanceUntilIdle()
        assertEquals(AddTrackState.SaveError, vm.state.value)

        fail = false
        vm.save(validForm)
        assertEquals(AddTrackState.Saving, vm.state.value)
        advanceUntilIdle()

        assertEquals("Plástico", (vm.state.value as AddTrackState.Added).track.name)
        assertEquals(listOf(100 to expectedRequest, 100 to expectedRequest), service.trackRequests)
    }

    @Test
    fun save_sendsTheTrackToTheAlbumOfTheSheet() = runTest(dispatcher) {
        val vm = viewModel(albumId = 103)

        vm.save(validForm)
        advanceUntilIdle()

        assertEquals(103, service.trackRequests.single().first)
    }
}
