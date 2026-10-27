package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasGridSpanCount
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.isCompletelyAbove
import co.edu.uniandes.vinilos.utils.itemAtPosition
import co.edu.uniandes.vinilos.utils.itemCount
import co.edu.uniandes.vinilos.utils.textAtPosition
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU01 - Consultar catálogo de álbumes, contra BackVynils.
 * Casos del inventario del Sprint 1: HU01-01, HU01-06 y HU01-08.
 *
 * Requisitos: BackVynils corriendo con álbumes (más de los que caben en una pantalla para HU01-08)
 * y las animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class AlbumListTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun waitForCatalog() {
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
    }

    /** HU01-01: el catálogo se muestra en 2 columnas y cada tarjeta tiene portada, nombre, artista, año y género. */
    @Test
    fun catalogShowsAlbumCardsInTwoColumns() {
        onView(withId(R.id.rv_albums)).check(matches(hasGridSpanCount(2)))

        onView(itemAtPosition(R.id.rv_albums, 0)).check(
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.iv_album_cover), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_album_name), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_album_artist), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_album_year_genre), isDisplayed()))
                )
            )
        )
    }

    /** HU01-06: al tocar una tarjeta se abre el detalle de ese álbum. */
    @Test
    fun tappingAlbumOpensItsDetail() {
        val albumName = textAtPosition(R.id.rv_albums, 0, R.id.tv_album_name)

        onView(withId(R.id.rv_albums))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.rv_albums)).check(doesNotExist())
        waitUntilDisplayed(withText(albumName))
    }

    /** HU01-08: al desplazarse hasta el final, el último álbum queda visible por encima del botón "Nuevo álbum". */
    @Test
    fun lastAlbumIsVisibleAboveNewAlbumButton() {
        val lastPosition = itemCount(R.id.rv_albums) - 1

        onView(withId(R.id.rv_albums))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(lastPosition))

        onView(itemAtPosition(R.id.rv_albums, lastPosition))
            .check(isCompletelyAbove(R.id.fab_new_album))
    }
}
