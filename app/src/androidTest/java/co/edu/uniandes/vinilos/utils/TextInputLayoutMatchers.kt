package co.edu.uniandes.vinilos.utils

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/** Un `TextInputLayout` cuya etiqueta (hint) es [expected]. */
fun hasHint(expected: String): Matcher<View> =
    textInputLayout("con la etiqueta \"$expected\"") { it.hint?.toString() == expected }

/** Un `TextInputLayout` cuyo texto de ayuda es [expected]. */
fun hasHelperText(expected: String): Matcher<View> =
    textInputLayout("con el texto de ayuda \"$expected\"") { it.helperText?.toString() == expected }

/** Un `TextInputLayout` que muestra el error [expected] bajo el campo. */
fun hasError(expected: String): Matcher<View> =
    textInputLayout("con el error \"$expected\"") { it.error?.toString() == expected }

private fun textInputLayout(label: String, check: (TextInputLayout) -> Boolean): Matcher<View> =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("TextInputLayout $label")
        }

        override fun matchesSafely(item: View) = item is TextInputLayout && check(item)
    }
