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
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas E2E de HU04 - Consultar la información detallada de un artista, contra BackVynils.
 * Casos del inventario del Sprint 2: HU04-01, HU04-02, HU04-03, HU04-04 y HU04-07, más los criterios
 * 3 ("Leer más") y 5 (abrir un álbum del carrusel) de la historia.
 *
 * Requisitos: BackVynils con sus datos iniciales (el músico Rubén Blades y la banda Queen, con sus
 * álbumes y premios), `scripts/seed-backvynils.sh` ejecutado (agrega a Freddie Mercury como integrante
 * de Queen) y las animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class ArtistDetailTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun openArtists() {
        onView(withId(R.id.artistListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_artists), hasItems()))
    }

    /** HU04-01: el detalle de un músico muestra foto, nombre, tipo, fecha de nacimiento y descripción, sin Integrantes. */
    @Test
    fun detailShowsArtistDataSheet() {
        openArtist(RUBEN_BLADES)

        onView(withId(R.id.iv_artist_image)).check(matches(isDisplayed()))
        onView(withId(R.id.chip_artist_type)).check(matches(withText(R.string.artist_type_musician)))
        onView(withId(R.id.chip_artist_date)).check(matches(withText("Nacido el 16 de julio de 1948")))
        onView(withId(R.id.tv_artist_description)).check(matches(isDisplayed()))
        onView(withId(R.id.section_artist_members)).check(matches(not(isDisplayed())))
    }

    /** HU04-02: los álbumes del artista se ven en el carrusel horizontal. */
    @Test
    fun detailShowsAlbumsCarousel() {
        openArtist(RUBEN_BLADES)

        onView(withId(R.id.rv_artist_albums)).perform(scrollTo()).check(matches(isDisplayed()))
        listOf("Poeta del pueblo", "Buscando América").forEach { album ->
            onView(withId(R.id.rv_artist_albums))
                .perform(scrollToItem<RecyclerView.ViewHolder>(hasDescendant(withText(album))))
            onView(allOf(withId(R.id.tv_artist_album_name), withText(album))).check(matches(isDisplayed()))
        }
    }

    /** Criterio 5 de HU04: tocar un álbum del carrusel abre su detalle. */
    @Test
    fun tappingAnAlbumOpensItsDetail() {
        openArtist(RUBEN_BLADES)

        onView(withId(R.id.rv_artist_albums))
            .perform(scrollTo())
            .perform(actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("Poeta del pueblo")), click()))

        waitUntilDisplayed(allOf(withId(R.id.tv_detail_name), withText("Poeta del pueblo")))
    }

    /** HU04-03: cada premio muestra su nombre, organización y año. */
    @Test
    fun detailShowsPrizes() {
        openArtist(RUBEN_BLADES)

        waitUntilDisplayed(withText("Grammy Award"))
        onView(withId(R.id.layout_artist_prizes)).perform(scrollTo()).check(
            matches(
                allOf(
                    hasDescendant(allOf(withId(R.id.tv_prize_name), withText("Grammy Award"))),
                    hasDescendant(
                        allOf(withId(R.id.tv_prize_organization), withText("National Academy of Recording Arts & Sciences"))
                    ),
                    hasDescendant(allOf(withId(R.id.tv_prize_year), withText("1978")))
                )
            )
        )
    }

    /** HU04-04: el detalle de una banda muestra tipo, fecha de fundación y la sección Integrantes con sus músicos. */
    @Test
    fun bandDetailShowsMembers() {
        openArtist(QUEEN)

        onView(withId(R.id.chip_artist_type)).check(matches(withText(R.string.artist_type_band)))
        onView(withId(R.id.chip_artist_date)).check(matches(withText("Fundada el 1 de enero de 1970")))
        onView(withId(R.id.section_artist_members)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.layout_artist_members))
            .check(matches(hasDescendant(allOf(withId(R.id.tv_member_name), withText("Freddie Mercury")))))
    }

    /** Criterio 3 de HU04: "Leer más" despliega la descripción completa y desaparece. */
    @Test
    fun readMoreExpandsTheDescription() {
        openArtist(QUEEN)

        waitUntilDisplayed(withId(R.id.btn_artist_read_more))
        onView(withId(R.id.tv_artist_description)).check(matches(not(showsFullText())))

        onView(withId(R.id.btn_artist_read_more)).perform(click())

        onView(withId(R.id.tv_artist_description)).check(matches(showsFullText()))
        onView(withId(R.id.btn_artist_read_more)).check(matches(not(isDisplayed())))
    }

    /** HU04-07: el botón atrás del sistema y la flecha de la barra regresan al listado. */
    @Test
    fun backNavigationReturnsToList() {
        openArtist(RUBEN_BLADES)
        pressBack()
        waitUntilDisplayed(withId(R.id.rv_artists))

        openArtist(QUEEN)
        onView(allOf(withContentDescription(R.string.action_back), isDisplayed())).perform(click())
        waitUntilDisplayed(withId(R.id.rv_artists))
    }

    /** Toca la tarjeta del artista [name] en el listado y espera a que cargue su detalle. */
    private fun openArtist(name: String) {
        onView(withId(R.id.rv_artists))
            .perform(actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(name)), click()))
        waitUntilDisplayed(allOf(withId(R.id.tv_artist_name), withText(name)))
    }

    private companion object {
        const val RUBEN_BLADES = "Rubén Blades Bellido de Luna"
        const val QUEEN = "Queen"
    }
}
