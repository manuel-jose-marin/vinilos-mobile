package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.BackVynilsApi
import co.edu.uniandes.vinilos.utils.hasError
import co.edu.uniandes.vinilos.utils.hasHelperText
import co.edu.uniandes.vinilos.utils.hasHint
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU08 - Asociar tracks con un álbum, contra BackVynils.
 * Cubren los criterios 1, 2, 3, 5, 8 y 11 que la estrategia de pruebas del Sprint 3 asigna a Espresso.
 *
 * Requisitos: BackVynils corriendo y las animaciones del dispositivo desactivadas. Cada prueba crea su propio álbum
 * por el API antes de abrir la app, así que no depende de los datos de otras pruebas, y al terminar borra el álbum
 * con sus tracks.
 */
@RunWith(AndroidJUnit4::class)
class AddTrackTest {

    private val albumName = "E2E tracks ${System.currentTimeMillis()}"
    private val trackName = "Track E2E ${System.currentTimeMillis()}"
    private var albumId = 0
    private lateinit var scenario: ActivityScenario<MainActivity>

    /** El álbum se crea antes de abrir la app, para que el catálogo ya lo traiga al cargar. */
    @Before
    fun openAlbumWithoutTracks() {
        albumId = BackVynilsApi.createAlbum(albumName)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
        onView(withId(R.id.rv_albums)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(albumName)), click())
        )
        waitUntilDisplayed(allOf(withId(R.id.tv_detail_name), withText(albumName)))
    }

    @After
    fun closeAppAndDeleteAlbum() {
        scenario.close()
        BackVynilsApi.deleteAlbum(albumId)
    }

    /** Criterios 1 y 2: "Agregar track" abre la hoja con el nombre del álbum y los campos con su texto de ayuda. */
    @Test
    fun addTrackOpensTheSheetWithTheAlbumNameAndRequiredFields() {
        openAddTrackSheet()

        onView(withId(R.id.tv_add_track_album)).check(matches(withText("Al álbum $albumName")))
        onView(withId(R.id.til_track_name)).check(
            matches(allOf(hasHint("Nombre del track"), hasHelperText("Obligatorio, como aparece en el álbum")))
        )
        onView(withId(R.id.til_track_duration)).check(
            matches(
                allOf(
                    hasHint("Duración (mm:ss)"),
                    hasHelperText("Obligatoria, minutos y segundos, por ejemplo 04:15")
                )
            )
        )
        onView(withId(R.id.btn_add_track_cancel)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_add_track_submit)).check(matches(isDisplayed()))
    }

    /** Criterio 3: con los campos vacíos se ve el error bajo cada uno, la hoja sigue abierta y no se envía nada. */
    @Test
    fun emptyFormShowsAnErrorUnderEachFieldAndIsNotSent() {
        openAddTrackSheet()

        onView(withId(R.id.btn_add_track_submit)).perform(click())

        onView(withId(R.id.til_track_name)).check(matches(hasError("El nombre del track es obligatorio")))
        onView(withId(R.id.til_track_duration)).check(matches(hasError("La duración es obligatoria")))
        onView(withId(R.id.btn_add_track_submit)).check(matches(isDisplayed()))
        assertEquals(0, BackVynilsApi.tracks(albumId).length())
    }

    /**
     * Criterio 5: una duración con formato inválido como "6.31" muestra su error y no se envía.
     * El teclado del campo no deja escribir el punto, así que la prueba pega el texto, como haría un usuario.
     */
    @Test
    fun durationWithInvalidFormatShowsErrorAndIsNotSent() {
        openAddTrackSheet()
        fillSheet(duration = "6.31")

        onView(withId(R.id.btn_add_track_submit)).perform(click())

        onView(withId(R.id.til_track_duration)).check(matches(hasError("Usa el formato mm:ss, por ejemplo 04:15")))
        onView(withId(R.id.btn_add_track_submit)).check(matches(isDisplayed()))
        assertEquals(0, BackVynilsApi.tracks(albumId).length())
    }

    /** Criterio 8: al agregar, la hoja se cierra, se ve el mensaje y la lista y el contador de tracks se actualizan. */
    @Test
    fun validTrackIsAddedAndTheListAndCounterAreUpdated() {
        onView(withId(R.id.tv_tracks_header)).perform(scrollTo()).check(matches(withText("Tracks · 0")))
        openAddTrackSheet()
        fillSheet(duration = "04:15")

        onView(withId(R.id.btn_add_track_submit)).perform(click())

        waitUntilDisplayed(withText("Track «$trackName» agregado"))
        onView(withId(R.id.btn_add_track_submit)).check(doesNotExist())
        onView(withId(R.id.tv_tracks_header)).perform(scrollTo()).check(matches(withText("Tracks · 1")))
        onView(allOf(withId(R.id.tv_track_name), withText(trackName))).perform(scrollTo()).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.tv_track_duration), withText("04:15"))).check(matches(isDisplayed()))

        val saved = BackVynilsApi.tracks(albumId)
        assertEquals(1, saved.length())
        assertEquals(trackName, saved.getJSONObject(0).getString("name"))
    }

    /** Criterio 11: "Cancelar" cierra la hoja sin agregar el track. */
    @Test
    fun cancelClosesTheSheetWithoutAddingTheTrack() {
        openAddTrackSheet()
        fillSheet(duration = "04:15")

        onView(withId(R.id.btn_add_track_cancel)).perform(click())

        waitUntilDisplayed(withId(R.id.tv_detail_name))
        onView(withId(R.id.btn_add_track_submit)).check(doesNotExist())
        onView(withId(R.id.tv_tracks_header)).perform(scrollTo()).check(matches(withText("Tracks · 0")))
        assertEquals(0, BackVynilsApi.tracks(albumId).length())
    }

    private fun openAddTrackSheet() {
        onView(withId(R.id.btn_add_track)).perform(scrollTo(), click())
        waitUntilDisplayed(withId(R.id.btn_add_track_submit))
    }

    private fun fillSheet(duration: String) {
        onView(withId(R.id.et_track_name)).perform(replaceText(trackName))
        onView(withId(R.id.et_track_duration)).perform(replaceText(duration))
        closeSoftKeyboard()
    }
}
