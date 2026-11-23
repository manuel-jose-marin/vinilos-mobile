package co.edu.uniandes.vinilos

import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import co.edu.uniandes.vinilos.validation.AlbumFormValidator
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL

/**
 * Pruebas E2E de HU07 - Crear un álbum, contra BackVynils.
 * Cubren los criterios 1, 2, 5, 6, 10 y 12 que la estrategia de pruebas del Sprint 3 asigna a Espresso.
 *
 * Requisitos: BackVynils corriendo y las animaciones del dispositivo desactivadas. Los álbumes que se crean usan el
 * prefijo "E2E " con una marca de tiempo y se borran al terminar cada prueba.
 */
@RunWith(AndroidJUnit4::class)
class AlbumCreateTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val albumName = "E2E ${System.currentTimeMillis()}"

    @Before
    fun openForm() {
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
        onView(withId(R.id.fab_new_album)).perform(click())
        waitUntilDisplayed(withId(R.id.btn_create_album_submit))
    }

    @After
    fun deleteCreatedAlbums() {
        albumIdsNamed(albumName).forEach { id ->
            val connection = URL("${BuildConfig.BASE_URL}albums/$id").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "DELETE"
                assertEquals(204, connection.responseCode)
            } finally {
                connection.disconnect()
            }
        }
    }

    /** Criterio 1: "Nuevo álbum" abre el formulario con los campos obligatorios marcados con "*". */
    @Test
    fun newAlbumOpensFormWithRequiredFields() {
        onView(withId(R.id.bottom_navigation)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        mapOf(
            R.id.til_album_name to "Nombre *",
            R.id.til_album_cover to "URL de la portada *",
            R.id.til_album_release_date to "Fecha de lanzamiento *",
            R.id.til_album_description to "Descripción *",
            R.id.til_album_genre to "Género *",
            R.id.til_album_record_label to "Sello discográfico *"
        ).forEach { (field, hint) -> onView(withId(field)).check(matches(hasHint(hint))) }
    }

    /** Criterio 2: Género y Sello muestran los valores que acepta el API. */
    @Test
    fun dropdownsShowTheValuesAcceptedByTheApi() {
        reveal(R.id.til_album_genre)
        onView(withId(R.id.actv_album_genre)).perform(click())
        AlbumFormValidator.GENRES.forEach {
            onView(withText(it)).inRoot(isPlatformPopup()).check(matches(isDisplayed()))
        }
        onView(withText(AlbumFormValidator.GENRES.first())).inRoot(isPlatformPopup()).perform(click())

        reveal(R.id.til_album_record_label)
        onView(withId(R.id.actv_album_record_label)).perform(click())
        AlbumFormValidator.RECORD_LABELS.forEach {
            onView(withText(it)).inRoot(isPlatformPopup()).check(matches(isDisplayed()))
        }
    }

    /** Criterio 5: con el formulario vacío se ve el error bajo cada campo y no se sale del formulario. */
    @Test
    fun emptyFormShowsAnErrorUnderEachFieldAndIsNotSent() {
        val albumsBefore = albums().length()

        onView(withId(R.id.btn_create_album_submit)).perform(click())

        mapOf(
            R.id.til_album_name to "El nombre es obligatorio",
            R.id.til_album_cover to "La URL de la portada es obligatoria",
            R.id.til_album_release_date to "La fecha de lanzamiento es obligatoria",
            R.id.til_album_description to "La descripción es obligatoria",
            R.id.til_album_genre to "Selecciona un género",
            R.id.til_album_record_label to "Selecciona un sello discográfico"
        ).forEach { (field, error) -> onView(withId(field)).check(matches(hasError(error))) }
        onView(withId(R.id.btn_create_album_submit)).check(matches(withText("Crear álbum")))
        assertEquals(albumsBefore, albums().length())
    }

    /** Criterio 6: una URL que no empieza por https:// muestra su error y el álbum no se crea. */
    @Test
    fun coverWithoutHttpsShowsErrorAndIsNotSent() {
        fillForm(cover = "http://example.com/portada.jpg")

        onView(withId(R.id.btn_create_album_submit)).perform(click())

        reveal(R.id.til_album_cover)
        onView(withId(R.id.til_album_cover)).check(matches(hasError("Ingresa una URL válida (https://…)")))
        assertTrue(albumIdsNamed(albumName).isEmpty())
    }

    /** Criterio 10: al confirmar vuelve al listado con el mensaje y el álbum aparece en la lista. */
    @Test
    fun validFormCreatesAlbumAndShowsItInTheList() {
        fillForm()

        onView(withId(R.id.btn_create_album_submit)).perform(click())

        waitUntilDisplayed(withText("Álbum «$albumName» creado"))
        onView(withId(R.id.btn_create_album_submit)).check(doesNotExist())
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
        onView(withId(R.id.rv_albums)).perform(
            RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(albumName)))
        )
        onView(allOf(withId(R.id.tv_album_name), withText(albumName))).check(matches(isDisplayed()))
        assertEquals(1, albumIdsNamed(albumName).size)
    }

    /** Criterio 12: "Cancelar" vuelve al listado sin crear el álbum. */
    @Test
    fun cancelReturnsToTheListWithoutCreating() {
        fillForm()

        onView(withId(R.id.btn_create_album_cancel)).perform(click())

        waitUntilDisplayed(withId(R.id.fab_new_album))
        onView(withId(R.id.btn_create_album_submit)).check(doesNotExist())
        assertTrue(albumIdsNamed(albumName).isEmpty())
    }

    /** Criterio 12: la flecha de regreso vuelve al listado sin crear el álbum. */
    @Test
    fun backArrowReturnsToTheListWithoutCreating() {
        fillForm()

        onView(withContentDescription(R.string.action_back)).perform(click())

        waitUntilDisplayed(withId(R.id.fab_new_album))
        onView(withId(R.id.btn_create_album_submit)).check(doesNotExist())
        assertTrue(albumIdsNamed(albumName).isEmpty())
    }

    private fun fillForm(cover: String = VALID_COVER) {
        reveal(R.id.til_album_name)
        onView(withId(R.id.et_album_name)).perform(replaceText(albumName))
        reveal(R.id.til_album_cover)
        onView(withId(R.id.et_album_cover)).perform(replaceText(cover))
        closeSoftKeyboard()
        reveal(R.id.til_album_release_date)
        onView(withId(R.id.et_album_release_date)).perform(click())
        onView(withId(com.google.android.material.R.id.confirm_button)).perform(click())
        reveal(R.id.til_album_description)
        onView(withId(R.id.et_album_description)).perform(replaceText("Álbum creado por una prueba E2E"))
        closeSoftKeyboard()
        reveal(R.id.til_album_genre)
        onView(withId(R.id.actv_album_genre)).perform(click())
        onView(withText("Rock")).inRoot(isPlatformPopup()).perform(click())
        reveal(R.id.til_album_record_label)
        onView(withId(R.id.actv_album_record_label)).perform(click())
        onView(withText("EMI")).inRoot(isPlatformPopup()).perform(click())
        closeSoftKeyboard()
    }

    /** Desplaza el formulario hasta el campo: en pantallas pequeñas no caben todos a la vez. */
    private fun reveal(fieldId: Int) {
        activityRule.scenario.onActivity { activity ->
            val field = activity.findViewById<View>(fieldId)
            activity.findViewById<NestedScrollView>(R.id.scroll_create_album).scrollTo(0, field.top)
        }
    }

    private fun albums(): JSONArray {
        val connection = URL("${BuildConfig.BASE_URL}albums").openConnection() as HttpURLConnection
        return try {
            JSONArray(connection.inputStream.bufferedReader().readText())
        } finally {
            connection.disconnect()
        }
    }

    private fun albumIdsNamed(name: String): List<Int> {
        val all = albums()
        return (0 until all.length()).map(all::getJSONObject).filter { it.getString("name") == name }.map { it.getInt("id") }
    }

    private fun hasHint(expected: String) = textInputLayout("con la ayuda \"$expected\"") { it.hint?.toString() == expected }

    private fun hasError(expected: String) = textInputLayout("con el error \"$expected\"") { it.error?.toString() == expected }

    private fun textInputLayout(label: String, check: (TextInputLayout) -> Boolean): Matcher<View> =
        object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("TextInputLayout $label")
            }

            override fun matchesSafely(item: View) = item is TextInputLayout && check(item)
        }

    private companion object {
        const val VALID_COVER = "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg"
    }
}
