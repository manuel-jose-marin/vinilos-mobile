package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.AccessibilityFinding
import co.edu.uniandes.vinilos.utils.auditAccessibility
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Revisión de accesibilidad (#69) de las 8 pantallas de HU01 a HU08 con Accessibility Test Framework, el motor de
 * Accessibility Scanner. Cada prueba abre una pantalla con datos reales de BackVynils y falla si hay errores; las
 * advertencias quedan en logcat (`adb logcat -s A11Y`) para revisarlas a mano.
 *
 * Requisitos: BackVynils con sus datos iniciales y las animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private fun assertNoErrors(findings: List<AccessibilityFinding>) {
        val errors = findings.filter { it.type == AccessibilityCheckResultType.ERROR }
        assertTrue("Errores de accesibilidad:\n" + errors.joinToString("\n"), errors.isEmpty())
    }

    private fun openAlbumList() {
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
    }

    private fun openAlbumDetail() {
        openAlbumList()
        onView(withId(R.id.rv_albums)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("Buscando América")), click())
        )
        waitUntilDisplayed(allOf(withId(R.id.tv_detail_name), withText("Buscando América")))
    }

    private fun openArtist(name: String) {
        onView(withId(R.id.artistListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_artists), hasItems()))
        onView(withId(R.id.rv_artists)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(startsWith(name))), click())
        )
        waitUntilDisplayed(allOf(withId(R.id.tv_artist_name), withText(startsWith(name))))
    }

    private fun openCollectorList() {
        onView(withId(R.id.collectorListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_collectors), hasItems()))
    }

    /** HU01: catálogo de álbumes con el botón "Nuevo álbum". */
    @Test
    fun albumList() {
        openAlbumList()
        assertNoErrors(auditAccessibility("Listado de álbumes"))
    }

    /** HU02: detalle de un álbum con tracks y comentarios. */
    @Test
    fun albumDetail() {
        openAlbumDetail()
        assertNoErrors(auditAccessibility("Detalle de álbum"))
        onView(withId(R.id.btn_add_track)).perform(scrollTo())
        assertNoErrors(auditAccessibility("Detalle de álbum (abajo)"))
    }

    /** HU08: hoja "Agregar track", vacía y con los errores de validación. */
    @Test
    fun addTrackSheet() {
        openAlbumDetail()
        onView(withId(R.id.btn_add_track)).perform(scrollTo(), click())
        waitUntilDisplayed(withId(R.id.et_track_name))
        assertNoErrors(auditAccessibility("Hoja Agregar track", isDialog()))

        onView(withId(R.id.btn_add_track_submit)).inRoot(isDialog()).perform(click())
        assertNoErrors(auditAccessibility("Hoja Agregar track con errores", isDialog()))
    }

    /** HU07: formulario de creación de álbum, vacío y con los errores de validación. */
    @Test
    fun createAlbumForm() {
        openAlbumList()
        onView(withId(R.id.fab_new_album)).perform(click())
        waitUntilDisplayed(withId(R.id.et_album_name))
        assertNoErrors(auditAccessibility("Formulario de álbum"))

        // "Crear álbum" está en la barra inferior, fuera del scroll; al fallar, el formulario sube al primer error.
        onView(withId(R.id.btn_create_album_submit)).perform(click())
        assertNoErrors(auditAccessibility("Formulario de álbum con errores"))
        onView(withId(R.id.til_album_record_label)).perform(scrollTo())
        assertNoErrors(auditAccessibility("Formulario de álbum con errores (abajo)"))
    }

    /** HU03: listado de artistas con sus filtros. */
    @Test
    fun artistList() {
        onView(withId(R.id.artistListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_artists), hasItems()))
        assertNoErrors(auditAccessibility("Listado de artistas"))
    }

    /** HU04: detalle de un músico. */
    @Test
    fun musicianDetail() {
        openArtist("Rubén Blades")
        assertNoErrors(auditAccessibility("Detalle de músico"))
    }

    /** HU04: detalle de una banda, con integrantes. */
    @Test
    fun bandDetail() {
        openArtist("Queen")
        assertNoErrors(auditAccessibility("Detalle de banda"))
    }

    /** HU05: listado de coleccionistas. */
    @Test
    fun collectorList() {
        openCollectorList()
        assertNoErrors(auditAccessibility("Listado de coleccionistas"))
    }

    /** HU06: detalle de un coleccionista, arriba (contacto y favoritos) y abajo (colección y comentarios). */
    @Test
    fun collectorDetail() {
        openCollectorList()
        onView(withId(R.id.rv_collectors)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("Manolo Bellon")), click())
        )
        waitUntilDisplayed(withId(R.id.scroll_collector_detail))
        assertNoErrors(auditAccessibility("Detalle de coleccionista"))
        onView(withId(R.id.tv_collector_comments_header)).perform(scrollTo())
        assertNoErrors(auditAccessibility("Detalle de coleccionista (abajo)"))
    }
}
