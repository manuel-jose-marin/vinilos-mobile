package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo as scrollToItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU06 - Consultar la información detallada de un coleccionista, contra BackVynils.
 * Casos del inventario del Sprint 3: criterios 1, 4, 5, 6, 7 y 8, y el paso del listado al detalle (HU05-07).
 * Los criterios 2, 3 y 9 (marcador, correo y error de conexión) se prueban a mano.
 *
 * Requisitos: BackVynils con sus datos iniciales (Manolo Bellon, con "Buscando América" a $35 disponible, su
 * favorito Rubén Blades y sus comentarios) y las animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class CollectorDetailTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun openManoloBellon() {
        onView(withId(R.id.collectorListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_collectors), hasItems()))
        onView(withId(R.id.rv_collectors)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("Manolo Bellon")), click())
        )
        waitUntilDisplayed(withId(R.id.scroll_collector_detail))
    }

    /** HU05-07 y criterio 1: tocar un coleccionista abre su detalle con iniciales, nombre, álbumes, teléfono y correo. */
    @Test
    fun detailShowsTheCollectorData() {
        onView(withId(R.id.tv_collector_detail_name)).check(matches(allOf(isDisplayed(), withText("Manolo Bellon"))))
        onView(withId(R.id.tv_collector_detail_initials)).check(matches(allOf(isDisplayed(), withText("MB"))))
        onView(withId(R.id.chip_collector_albums_count)).check(matches(allOf(isDisplayed(), withText("1 álbum"))))
        onView(withId(R.id.btn_collector_phone)).check(matches(allOf(isDisplayed(), withText("3502457896"))))
        onView(withId(R.id.btn_collector_email)).check(matches(allOf(isDisplayed(), withText("manollo@caracol.com.co"))))
    }

    /** Criterio 4: Rubén Blades aparece entre los artistas favoritos. */
    @Test
    fun favoritePerformersShowRubenBlades() {
        onView(withId(R.id.tv_collector_performers_empty)).check(matches(org.hamcrest.Matchers.not(isDisplayed())))
        onView(withId(R.id.rv_collector_performers)).perform(
            scrollToItem<RecyclerView.ViewHolder>(hasDescendant(withText(startsWith("Rubén Blades"))))
        )
        onView(allOf(withId(R.id.tv_collector_performer_name), withText(startsWith("Rubén Blades"))))
            .check(matches(isDisplayed()))
    }

    /** Criterio 5: tocar un favorito abre el detalle del artista. */
    @Test
    fun tappingAFavoriteOpensTheArtistDetail() {
        onView(withId(R.id.rv_collector_performers)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(startsWith("Rubén Blades"))), click())
        )

        waitUntilDisplayed(allOf(withId(R.id.tv_artist_name), withText(startsWith("Rubén Blades"))))
    }

    /** Criterio 6: la colección muestra Buscando América con su artista, año, precio en COP y estado. */
    @Test
    fun collectionShowsTheAlbumWithPriceAndStatus() {
        onView(withId(R.id.tv_collector_albums_header)).perform(scrollTo()).check(matches(withText("Colección · 1")))
        onView(withId(R.id.tv_collector_album_name)).perform(scrollTo()).check(matches(withText("Buscando América")))
        onView(withId(R.id.tv_collector_album_artist)).check(matches(withText(startsWith("Rubén Blades"))))
        onView(withId(R.id.tv_collector_album_year)).check(matches(withText("1984")))
        onView(withId(R.id.tv_collector_album_price)).check(matches(withText("$35 COP")))
        onView(withId(R.id.chip_collector_album_status)).check(matches(withText("Disponible")))
        onView(withId(R.id.iv_collector_album_cover)).check(matches(isDisplayed()))
    }

    /** Criterio 7: tocar un álbum de la colección abre su detalle. */
    @Test
    fun tappingAnAlbumOpensItsDetail() {
        onView(withId(R.id.card_collector_album)).perform(scrollTo(), click())

        waitUntilDisplayed(allOf(withId(R.id.tv_detail_name), withText("Buscando América")))
    }

    /** Criterio 8: los comentarios se ven con su calificación, sin opción de escribir. */
    @Test
    fun commentsAreShownReadOnly() {
        onView(withId(R.id.tv_collector_comments_header)).perform(scrollTo()).check(matches(withText("Comentarios · 2")))
        onView(withText("Un clásico de la salsa.")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("The most relevant album of Ruben Blades")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    /** Volver con la flecha regresa al listado de coleccionistas. */
    @Test
    fun backReturnsToTheCollectorList() {
        pressBack()

        waitUntilDisplayed(allOf(withId(R.id.rv_collectors), hasItems()))
    }
}
