package co.edu.uniandes.vinilos.utils

import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

/** Cumple si el RecyclerView tiene al menos un ítem. */
fun hasItems(): Matcher<View> = object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("RecyclerView con al menos un ítem")
    }

    override fun matchesSafely(recyclerView: RecyclerView): Boolean =
        (recyclerView.adapter?.itemCount ?: 0) > 0
}

/** Cumple si el RecyclerView usa una cuadrícula de [spanCount] columnas. */
fun hasGridSpanCount(spanCount: Int): Matcher<View> =
    object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("RecyclerView en cuadrícula de $spanCount columnas")
        }

        override fun matchesSafely(recyclerView: RecyclerView): Boolean =
            (recyclerView.layoutManager as? GridLayoutManager)?.spanCount == spanCount
    }

/** Número de ítems del RecyclerView con id [recyclerViewId]. */
fun itemCount(@IdRes recyclerViewId: Int): Int {
    var count = 0
    onView(withId(recyclerViewId)).perform(readRecyclerView { count = it.adapter?.itemCount ?: 0 })
    return count
}

/** Texto del [TextView] con id [childId] dentro del ítem en [position]; el ítem debe estar en pantalla. */
fun textAtPosition(@IdRes recyclerViewId: Int, position: Int, @IdRes childId: Int): String {
    var text = ""
    onView(withId(recyclerViewId)).perform(readRecyclerView { recyclerView ->
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            ?: throw AssertionError("El ítem $position no está en pantalla")
        text = itemView.findViewById<TextView>(childId).text.toString()
    })
    return text
}

/** Vista del ítem en [position], para verificar sus elementos con `check`. */
fun itemAtPosition(@IdRes recyclerViewId: Int, position: Int): Matcher<View> =
    object : BoundedMatcher<View, View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("ítem en la posición $position del RecyclerView")
        }

        override fun matchesSafely(view: View): Boolean {
            val recyclerView = view.rootView.findViewById<RecyclerView>(recyclerViewId) ?: return false
            return recyclerView.findViewHolderForAdapterPosition(position)?.itemView === view
        }
    }

private fun readRecyclerView(read: (RecyclerView) -> Unit): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())

    override fun getDescription(): String = "leer el estado del RecyclerView"

    override fun perform(uiController: UiController, view: View) {
        read(view as RecyclerView)
    }
}
