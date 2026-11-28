package co.edu.uniandes.vinilos

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniandes.vinilos.ui.MainActivity
import co.edu.uniandes.vinilos.utils.hasItems
import co.edu.uniandes.vinilos.utils.waitUntilDisplayed
import com.google.android.material.color.MaterialColors
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de los defectos de accesibilidad encontrados en la revisión del Sprint 3 (#69). Cada una fallaba antes de
 * su corrección. Requisitos: BackVynils con sus datos iniciales y las animaciones del dispositivo desactivadas.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityFixesTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun themed(fontScale: Float = 1f) = ContextThemeWrapper(
        context.createConfigurationContext(Configuration(context.resources.configuration).apply { this.fontScale = fontScale }),
        R.style.Theme_Vinilos
    )

    private fun isAccessibilityHeading(): Matcher<View> = object : BoundedMatcher<View, View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("marcado como encabezado para TalkBack")
        }

        // ViewCompat lee la marca en todas las versiones; View.isAccessibilityHeading solo existe desde API 28.
        override fun matchesSafely(view: View) = ViewCompat.isAccessibilityHeading(view)
    }

    /** Las flechas de Género y Sello dicen qué lista abren, en vez de "Mostrar menú desplegable" en ambas. */
    @Test
    fun createAlbumForm_dropdownIconsSayWhichListTheyOpen() {
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
        onView(withId(R.id.fab_new_album)).perform(click())
        waitUntilDisplayed(withId(R.id.et_album_name))

        onView(withContentDescription("Ver los géneros")).check(matches(isDisplayed()))
        onView(withContentDescription("Ver los sellos discográficos")).check(matches(isDisplayed()))
    }

    /** Teléfono y correo anuncian la acción y el dato, no solo el número o la dirección. */
    @Test
    fun collectorDetail_contactButtonsSayTheAction() {
        onView(withId(R.id.collectorListFragment)).perform(click())
        waitUntilDisplayed(allOf(withId(R.id.rv_collectors), hasItems()))
        onView(withId(R.id.rv_collectors)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("Manolo Bellon")), click())
        )
        waitUntilDisplayed(withId(R.id.scroll_collector_detail))

        onView(withId(R.id.btn_collector_phone))
            .check(matches(withContentDescription("Llamar a Manolo Bellon al 3502457896")))
        onView(withId(R.id.btn_collector_email))
            .check(matches(withContentDescription("Escribir un correo a Manolo Bellon a manollo@caracol.com.co")))
        onView(withId(R.id.tv_collector_albums_header)).check(matches(isAccessibilityHeading()))
        onView(withId(R.id.tv_collector_comments_header)).check(matches(isAccessibilityHeading()))
    }

    /** Los títulos de sección del detalle de álbum son encabezados, para saltar entre secciones con TalkBack. */
    @Test
    fun albumDetail_sectionTitlesAreHeadings() {
        waitUntilDisplayed(allOf(withId(R.id.rv_albums), hasItems()))
        onView(withId(R.id.rv_albums)).perform(
            actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("Buscando América")), click())
        )
        waitUntilDisplayed(withId(R.id.tv_detail_name))

        onView(withId(R.id.tv_tracks_header)).check(matches(isAccessibilityHeading()))
        onView(withId(R.id.tv_comments_header)).check(matches(isAccessibilityHeading()))
    }

    /** Las estrellas de los comentarios tienen al menos 3:1 frente al fondo (WCAG 1.4.11); el ámbar daba 2.83:1. */
    @Test
    fun commentStars_haveEnoughContrastAgainstTheSurface() {
        val theme = themed()
        val rating = LayoutInflater.from(theme).inflate(R.layout.item_comment, null)
            .findViewById<RatingBar>(R.id.rating_comment)
        val stars = rating.progressTintList!!.defaultColor
        val surface = MaterialColors.getColor(theme, com.google.android.material.R.attr.colorSurface, 0)

        val contrast = ColorUtils.calculateContrast(stars, surface)
        assertTrue("Contraste de las estrellas: %.2f".format(contrast), contrast >= 3.0)
    }

    /** Con el texto al 200 %, un nombre largo de artista se ve en dos líneas y no se encima con el número de álbumes. */
    @Test
    fun artistRow_growsInsteadOfOverlappingWithLargeText() {
        val row = LayoutInflater.from(themed(fontScale = 2f)).inflate(R.layout.item_artist, null)
        val name = row.findViewById<TextView>(R.id.tv_artist_name)
        val count = row.findViewById<TextView>(R.id.tv_artist_albums_count)
        name.text = "Freddie Mercury"
        count.text = "0 álbumes"
        row.findViewById<TextView>(R.id.chip_artist_type).text = "Músico"

        val width = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
        row.measure(width, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        row.layout(0, 0, row.measuredWidth, row.measuredHeight)

        assertEquals("El nombre no debe cortarse con «…»", 0, name.layout.getEllipsisCount(name.lineCount - 1))
        assertTrue("El nombre (abajo en ${name.bottom}) se encima con el número de álbumes (${count.top})", name.bottom <= count.top)
    }
}
