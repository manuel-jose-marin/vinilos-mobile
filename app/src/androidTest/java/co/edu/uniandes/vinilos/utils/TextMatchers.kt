package co.edu.uniandes.vinilos.utils

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/**
 * Cumple si el [TextView] muestra su texto completo: ninguna línea termina en puntos suspensivos
 * y la última línea llega hasta el final del texto (no hay un límite de líneas que lo recorte).
 */
fun showsFullText(): Matcher<View> = object : BoundedMatcher<View, TextView>(TextView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("TextView que muestra su texto completo, sin cortarse")
    }

    override fun matchesSafely(textView: TextView): Boolean {
        val layout = textView.layout ?: return false
        val noEllipsis = (0 until layout.lineCount).all { layout.getEllipsisCount(it) == 0 }
        return noEllipsis && layout.getLineEnd(layout.lineCount - 1) == textView.text.length
    }
}
