package co.edu.uniandes.vinilos.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import co.edu.uniandes.vinilos.fakes.FakeAlbumService
import co.edu.uniandes.vinilos.fakes.FakeAlbumService.Companion.album
import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.NewAlbum
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.validation.AlbumField
import co.edu.uniandes.vinilos.validation.AlbumForm
import co.edu.uniandes.vinilos.validation.AlbumFormError
import co.edu.uniandes.vinilos.validation.AlbumFormValidator
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
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAlbumViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private val defaultTimeZone = TimeZone.getDefault()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // La zona del teléfono en Colombia; la fecha enviada no debe correrse al día anterior.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        TimeZone.setDefault(defaultTimeZone)
    }

    private val service = FakeAlbumService()

    /** 1995-11-01 a la medianoche UTC, como lo entrega el selector de fecha. */
    private val nov1995 = 815_184_000_000L

    /** "Hoy" fijo para las pruebas: 2026-11-21 a la medianoche UTC. */
    private val today = 1_795_219_200_000L

    private val validForm = AlbumForm(
        name = "  Siembra  ",
        cover = " https://portada/siembra.jpg ",
        releaseDate = nov1995,
        description = " Álbum de salsa ",
        genre = "Salsa",
        recordLabel = "Fania Records"
    )

    private val expectedRequest = NewAlbum(
        name = "Siembra",
        cover = "https://portada/siembra.jpg",
        releaseDate = "1995-11-01",
        description = "Álbum de salsa",
        genre = "Salsa",
        recordLabel = "Fania Records"
    )

    private fun viewModel() =
        CreateAlbumViewModel(AlbumRepository(service, FakeCollectorService()), AlbumFormValidator { today })

    @Test
    fun initialState_isEditingWithoutErrors() {
        assertEquals(CreateAlbumState.Editing(), viewModel().state.value)
    }

    @Test
    fun save_withAnInvalidForm_showsTheErrorOfEachFieldAndSendsNothing() = runTest(dispatcher) {
        val vm = viewModel()

        vm.save(validForm.copy(name = " ", cover = "http://portada.jpg", releaseDate = today + DAY))
        advanceUntilIdle()

        val expected = mapOf(
            AlbumField.NAME to AlbumFormError.NAME_REQUIRED,
            AlbumField.COVER to AlbumFormError.COVER_INVALID,
            AlbumField.RELEASE_DATE to AlbumFormError.RELEASE_DATE_FUTURE
        )
        assertEquals(CreateAlbumState.Editing(expected), vm.state.value)
        assertTrue(service.createRequests.isEmpty())
    }

    @Test
    fun save_withAnEmptyForm_marksEveryRequiredField() = runTest(dispatcher) {
        val vm = viewModel()

        vm.save(AlbumForm())

        val errors = (vm.state.value as CreateAlbumState.Editing).errors
        assertEquals(AlbumField.entries.toSet(), errors.keys)
        assertTrue(service.createRequests.isEmpty())
    }

    @Test
    fun save_withAValidForm_showsSavingAndThenTheCreatedAlbum() = runTest(dispatcher) {
        val created = album(101, "Siembra")
        service.created = { created }
        val vm = viewModel()

        vm.save(validForm)
        assertEquals(CreateAlbumState.Saving, vm.state.value)
        advanceUntilIdle()

        assertEquals(CreateAlbumState.Saved(created), vm.state.value)
        assertEquals(listOf(expectedRequest), service.createRequests)
    }

    @Test
    fun save_whenTheFormWasCorrected_sendsItAfterTheErrors() = runTest(dispatcher) {
        val vm = viewModel()
        vm.save(validForm.copy(genre = null))
        assertTrue(vm.state.value is CreateAlbumState.Editing)

        vm.save(validForm)
        advanceUntilIdle()

        assertTrue(vm.state.value is CreateAlbumState.Saved)
        assertEquals(listOf(expectedRequest), service.createRequests)
    }

    /** Criterio 9 de HU07: un doble toque en "Crear álbum" mientras se guarda no crea dos álbumes. */
    @Test
    fun save_whileSaving_doesNotSendTheAlbumTwice() = runTest(dispatcher) {
        val answer = CompletableDeferred<Album>()
        service.created = { answer.await() }
        val vm = viewModel()

        vm.save(validForm)
        runCurrent()
        vm.save(validForm)
        vm.save(validForm.copy(name = "Otro"))
        runCurrent()
        assertEquals(CreateAlbumState.Saving, vm.state.value)

        answer.complete(album(101, "Siembra"))
        advanceUntilIdle()

        assertEquals(1, service.createRequests.size)
        assertTrue(vm.state.value is CreateAlbumState.Saved)
    }

    @Test
    fun save_afterTheAlbumWasCreated_doesNothing() = runTest(dispatcher) {
        val vm = viewModel()
        vm.save(validForm)
        advanceUntilIdle()
        val saved = vm.state.value

        vm.save(validForm)
        advanceUntilIdle()

        assertEquals(saved, vm.state.value)
        assertEquals(1, service.createRequests.size)
    }

    /** Criterio 11 de HU07: con un error del servidor, el usuario puede reintentar con los mismos datos. */
    @Test
    fun save_whenTheServerFails_showsSaveErrorAndAllowsRetrying() = runTest(dispatcher) {
        var fail = true
        service.created = { if (fail) throw VolleyError("sin conexión") else album(101, it.name) }
        val vm = viewModel()

        vm.save(validForm)
        advanceUntilIdle()
        assertEquals(CreateAlbumState.SaveError, vm.state.value)

        fail = false
        vm.save(validForm)
        assertEquals(CreateAlbumState.Saving, vm.state.value)
        advanceUntilIdle()

        assertEquals("Siembra", (vm.state.value as CreateAlbumState.Saved).album.name)
        assertEquals(listOf(expectedRequest, expectedRequest), service.createRequests)
    }

    @Test
    fun save_withAReleaseDateOfToday_isAccepted() = runTest(dispatcher) {
        val vm = viewModel()

        vm.save(validForm.copy(releaseDate = today))
        advanceUntilIdle()

        assertTrue(vm.state.value is CreateAlbumState.Saved)
        assertEquals("2026-11-21", service.createRequests.single().releaseDate)
    }

    @Test
    fun releaseDateText_keepsTheChosenDayInAZoneBehindUtc() {
        assertEquals("1995-11-01", CreateAlbumViewModel.releaseDateText(nov1995))
        assertEquals("2000-01-01", CreateAlbumViewModel.releaseDateText(946_684_800_000L))
    }

    private companion object {
        const val DAY = 24 * 60 * 60 * 1000L
    }
}
