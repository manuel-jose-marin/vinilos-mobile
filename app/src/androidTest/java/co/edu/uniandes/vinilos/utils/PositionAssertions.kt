package co.edu.uniandes.vinilos.utils

import android.graphics.Rect
import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.ViewAssertion

/**
 * Verifica que la vista se vea completa en pantalla y que su borde inferior quede por encima
 * del borde superior de la vista con id [otherViewId] (por ejemplo, un botón flotante).
 */
fun isCompletelyAbove(@IdRes otherViewId: Int): ViewAssertion = ViewAssertion { view, noViewFoundException ->
    if (noViewFoundException != null) throw noViewFoundException

    val other = view.rootView.findViewById<View>(otherViewId)
        ?: throw AssertionError("No existe la vista de referencia con id $otherViewId")

    val visible = Rect()
    val fullyVisible = view.getGlobalVisibleRect(visible) && visible.height() == view.height
    if (!fullyVisible) throw AssertionError("La vista no se ve completa en pantalla")

    val viewBottom = IntArray(2).also { view.getLocationOnScreen(it) }[1] + view.height
    val otherTop = IntArray(2).also { other.getLocationOnScreen(it) }[1]
    if (viewBottom > otherTop) {
        throw AssertionError("La vista termina en y=$viewBottom, por debajo del inicio de la otra vista (y=$otherTop)")
    }
}
