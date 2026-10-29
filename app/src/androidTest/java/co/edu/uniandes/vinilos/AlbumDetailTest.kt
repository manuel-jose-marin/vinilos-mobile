package co.edu.uniandes.vinilos

import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.showsFullText
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU02 - Consultar la información detallada de un álbum, contra BackVynils.
 * Casos del inventario del Sprint 1: HU02-01, HU02-02, HU02-03 y HU02-07.
 *
 * Requisitos: BackVynils corriendo con los datos de `scripts/seed-backvynils.sh` (el álbum
 * "Buscando América" de la base inicial y "Siembra", con 3 tracks y 1 comentario) y las
 * animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class AlbumDetailTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun waitForCatalog() {
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
    }

    /** HU02-01: el detalle muestra portada, nombre, artista, fecha de lanzamiento, género, sello y descripción. */
    @Test
    fun detailShowsTheAlbumDataSheet() {
        openAlbumFromCatalog("Buscando América")

        onView(withId(R.id.iv_detail_cover)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_detail_name)).check(matches(withText("Buscando América")))
        onView(withId(R.id.tv_detail_artist)).check(matches(withText("Rubén Blades Bellido de Luna")))
        onView(withId(R.id.chip_release_date)).check(matches(withText("1 de agosto de 1984")))
        onView(withId(R.id.chip_genre)).check(matches(withText("Salsa")))
        onView(withId(R.id.chip_record_label)).check(matches(withText("Elektra")))
        onView(withId(R.id.tv_detail_description))
            .check(matches(allOf(isDisplayed(), withText(startsWith("Buscando América es el primer álbum")))))
    }

    /** HU02-02: "Tracks · N" con número, nombre y duración de cada track; el nombre largo se ve completo. */
    @Test
    fun detailShowsTracksWithNumberNameAndDuration() {
        openAlbumFromCatalog("Siembra")

        onView(withId(R.id.tv_tracks_header)).perform(scrollTo()).check(matches(withText("Tracks · 3")))

        // Número y duración de cada track, en el orden en que los devuelve el servidor
        for ((number, duration) in listOf("1" to "6:52", "2" to "7:21", "3" to "5:52")) {
            onView(allOf(withId(R.id.tv_track_number), withText(number)))
                .perform(scrollTo()).check(matches(isDisplayed()))
            onView(allOf(withId(R.id.tv_track_duration), withText(duration)))
                .perform(scrollTo()).check(matches(isDisplayed()))
        }
        for (name in listOf("Plástico", "Siembra")) {
            onView(allOf(withId(R.id.tv_track_name), withText(name)))
                .perform(scrollTo()).check(matches(isDisplayed()))
        }

        onView(allOf(withId(R.id.tv_track_name), withText(startsWith("Pedro Navaja"))))
            .perform(scrollTo())
            .check(matches(allOf(isDisplayed(), showsFullText())))
    }

    /** HU02-03: cada comentario muestra autor, calificación y texto, y no hay dónde escribir uno. */
    @Test
    fun detailShowsCommentsWithAuthorRatingAndText() {
        openAlbumFromCatalog("Siembra")

        onView(withId(R.id.tv_comments_header)).perform(scrollTo()).check(matches(withText("Comentarios · 1")))
        onView(withId(R.id.tv_comment_author)).perform(scrollTo()).check(matches(withText("Manolo Bellon")))
        onView(withId(R.id.rating_comment))
            .perform(scrollTo())
            .check(matches(withContentDescription("Calificación: 4 de 5")))
        onView(withId(R.id.tv_comment_text)).perform(scrollTo()).check(matches(withText("Un clásico de la salsa.")))

        onView(isAssignableFrom(EditText::class.java)).check(doesNotExist())
    }

    /** HU02-07: la flecha de regreso y el botón atrás del sistema vuelven al catálogo. */
    @Test
    fun backNavigationReturnsToTheCatalog() {
        openAlbumFromCatalog("Buscando América")
        onView(withContentDescription(R.string.action_back)).perform(click())
        waitUntilDisplayed(withId(R.id.rv_albums))

        openAlbumFromCatalog("Poeta del pueblo")
        pressBack()
        waitUntilDisplayed(withId(R.id.rv_albums))
    }

    /** Toca en el catálogo la tarjeta del álbum [name] y espera a que cargue su detalle. */
    private fun openAlbumFromCatalog(name: String) {
        onView(withId(R.id.rv_albums)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(name)), click())
        )
        waitUntilDisplayed(allOf(withId(R.id.tv_detail_name), withText(name)))
    }
}
