package co.edu.uniandes.vinilos

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.itemAtPosition
import co.edu.uniandes.vinilos.utils.itemCount
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU05 - Consultar listado de coleccionistas, contra BackVynils.
 * Caso del inventario del Sprint 2: HU05-01.
 *
 * Requisitos: BackVynils corriendo con sus datos iniciales (incluye a "Manolo Bellon") y las
 * animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class CollectorListTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun openCollectors() {
        onView(withId(R.id.collectorListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_collectors), hasItems()))
    }

    /** HU05-01: cada fila muestra iniciales, nombre, correo y número de álbumes. */
    @Test
    fun listShowsCollectors() {
        onView(itemAtPosition(R.id.rv_collectors, 0)).check(
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.tv_collector_initials), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_collector_name), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_collector_email), isDisplayed())),
                    hasDescendant(allOf(withId(R.id.tv_collector_albums_count), isDisplayed()))
                )
            )
        )
    }

    /** HU05-01: los datos de un coleccionista conocido coinciden con los de BackVynils. */
    @Test
    fun collectorRowShowsTheServerData() {
        val manolo = hasDescendant(withText("Manolo Bellon"))
        onView(withId(R.id.rv_collectors)).perform(scrollTo<RecyclerView.ViewHolder>(manolo))

        onView(allOf(withId(R.id.card_collector), manolo)).check(
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.tv_collector_initials), withText("MB"))),
                    hasDescendant(allOf(withId(R.id.tv_collector_email), withText("manollo@caracol.com.co"))),
                    hasDescendant(allOf(withId(R.id.tv_collector_albums_count), withText("1 álbum")))
                )
            )
        )
    }

    /** HU05-01: se muestra el total de coleccionistas y coincide con las filas de la lista. */
    @Test
    fun listShowsTheTotalNumberOfCollectors() {
        val total = itemCount(R.id.rv_collectors)
        val expected = InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getQuantityString(R.plurals.collectors_count, total, total)

        onView(withId(R.id.tv_collectors_count)).check(matches(allOf(isDisplayed(), withText(expected))))
    }
}
