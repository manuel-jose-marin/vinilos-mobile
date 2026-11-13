package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.itemAtPosition
import co.edu.uniandes.vinilos.utils.textAtPosition
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU03 - Consultar el listado de artistas, contra BackVynils.
 * Casos del inventario del Sprint 2: HU03-01, HU03-02 y HU03-06.
 *
 * Requisitos: BackVynils corriendo con datos de artistas (músicos y bandas)
 * y las animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class ArtistListTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun navigateToArtistsTab() {
        onView(withId(R.id.artistListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_artists), hasItems()))
    }

    /** HU03-01: el listado muestra las tarjetas de artistas con avatar, nombre, tipo y número de álbumes. */
    @Test
    fun listShowsMusiciansAndBands() {
        onView(itemAtPosition(R.id.rv_artists, 0)).check(
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.iv_artist_image), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_artist_name), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.chip_artist_type), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_artist_albums_count), isDisplayed()))
                )
            )
        )
    }

    /** HU03-02: los botones de filtro (Todos, Músicos, Bandas) filtran la lista en memoria. */
    @Test
    fun filteringByMusiciansOrBands() {
        onView(withId(R.id.btn_filter_musicians)).perform(click())
        onView(itemAtPosition(R.id.rv_artists, 0)).check(
            matches(hasDescendant(allOf(withId(R.id.chip_artist_type), withText(R.string.artist_type_musician))))
        )

        onView(withId(R.id.btn_filter_bands)).perform(click())
        onView(itemAtPosition(R.id.rv_artists, 0)).check(
            matches(hasDescendant(allOf(withId(R.id.chip_artist_type), withText(R.string.artist_type_band))))
        )

        onView(withId(R.id.btn_filter_all)).perform(click())
        onView(withId(R.id.rv_artists)).check(matches(hasItems()))
    }

    /** HU03-06: al tocar la tarjeta de un artista se abre su pantalla de detalle. */
    @Test
    fun tappingArtistOpensDetail() {
        val artistName = textAtPosition(R.id.rv_artists, 0, R.id.tv_artist_name)

        onView(withId(R.id.rv_artists))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        onView(withId(R.id.rv_artists)).check(doesNotExist())
        waitUntilDisplayed(withText(artistName))
    }
}
