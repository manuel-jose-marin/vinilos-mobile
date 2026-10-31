package co.edu.uniandes.vinilos.utils

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

/** Tiempo máximo de espera para respuestas de BackVynils en las pruebas E2E. */
const val NETWORK_TIMEOUT_MS = 10_000L
private const val POLL_INTERVAL_MS = 200L

/**
 * Espera hasta que al menos una vista que cumpla [matcher] esté visible en pantalla.
 * Si varias vistas cumplen el matcher (por ejemplo, el mismo texto en la barra y en el contenido), basta con una.
 *
 * Espresso no sincroniza las peticiones de Volley, así que las pruebas E2E que dependen del
 * backend usan esta espera en lugar de pausas fijas. Falla con el último error si se agota [timeoutMs].
 */
fun waitUntilDisplayed(matcher: Matcher<View>, timeoutMs: Long = NETWORK_TIMEOUT_MS) {
    val deadline = System.currentTimeMillis() + timeoutMs
    var lastError: Throwable
    do {
        try {
            onView(isRoot()).check(matches(hasDescendant(allOf(matcher, isDisplayed()))))
            return
        } catch (error: NoMatchingViewException) {
            lastError = error
        } catch (error: AssertionFailedError) {
            lastError = error
        }
        Thread.sleep(POLL_INTERVAL_MS)
    } while (System.currentTimeMillis() < deadline)
    throw AssertionError("La vista no apareció en $timeoutMs ms: $matcher", lastError)
}
