package co.edu.uniandes.vinilos.ui

import android.view.View
import androidx.core.view.ViewCompat

/**
 * Marca títulos de sección como encabezados para que TalkBack permita saltar entre ellos. Se hace con `ViewCompat`
 * porque el atributo `android:accessibilityHeading` solo existe desde Android 9 (API 28) y la app soporta desde
 * Android 5.0 (API 21); en versiones anteriores `ViewCompat` lo expone a TalkBack con un delegado de accesibilidad.
 */
fun markAsHeadings(vararg titles: View) {
    titles.forEach { ViewCompat.setAccessibilityHeading(it, true) }
}
