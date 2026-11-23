package co.edu.uniandes.vinilos.utils

import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage
import org.hamcrest.Matcher
import java.util.Locale

/** Un resultado de Accessibility Test Framework (el motor de Accessibility Scanner) en una pantalla. */
data class AccessibilityFinding(
    val screen: String,
    val type: AccessibilityCheckResultType,
    val check: String,
    val view: String,
    val message: String
) {
    override fun toString() = "[$screen] $type $check en $view: $message"
}

/**
 * Revisa la pantalla actual con todas las reglas de Accessibility Test Framework: etiquetas para lectores de pantalla,
 * áreas táctiles de 48 dp, contraste de texto e imágenes (con una captura de pantalla), orden de lectura, textos
 * repetidos y más. Devuelve los errores y advertencias, y los escribe en logcat con la etiqueta `A11Y`.
 *
 * @param root ventana a revisar; por ejemplo, `isDialog()` para la hoja inferior. Por defecto, la ventana con foco.
 */
fun auditAccessibility(screen: String, root: Matcher<Root>? = null): List<AccessibilityFinding> {
    // La captura se toma fuera del hilo principal; la revisión de la jerarquía, dentro de él. Si la pantalla se
    // reacomoda entre las dos (por ejemplo, cuando Glide termina de cargar una portada), ATF compara colores de
    // posiciones que ya no coinciden y reporta contrastes falsos. Por eso se espera a que la interfaz esté quieta,
    // y las advertencias de contraste se confirman midiendo la captura antes de tratarlas como defectos.
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.waitForIdleSync()
    Thread.sleep(SETTLE_MS)
    instrumentation.waitForIdleSync()
    val screenshot = instrumentation.uiAutomation.takeScreenshot()
    val findings = mutableListOf<AccessibilityFinding>()

    val interaction = onView(isRoot()).let { if (root != null) it.inRoot(root) else it }
    interaction.perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()

        override fun getDescription() = "revisar la accesibilidad de $screen"

        override fun perform(uiController: UiController, view: View) {
            val hierarchy = AccessibilityHierarchyAndroid.newBuilder(view).build()
            val parameters = Parameters().apply { screenshot?.let { putScreenCapture(BitmapImage(it)) } }
            AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
                .flatMap { it.runCheckOnHierarchy(hierarchy, null, parameters) }
                .filter { it.type == AccessibilityCheckResultType.ERROR || it.type == AccessibilityCheckResultType.WARNING }
                .mapTo(findings) { result ->
                    val element = result.element
                    val name = element?.resourceName?.substringAfter(":id/")
                        ?: element?.className?.toString()?.substringAfterLast('.')
                        ?: "?"
                    val text = (element?.text ?: element?.contentDescription)?.toString()?.take(40)
                    AccessibilityFinding(
                        screen = screen,
                        type = result.type,
                        check = result.sourceCheckClass.simpleName,
                        view = if (text.isNullOrBlank()) name else "$name «$text»",
                        message = result.getMessage(Locale.getDefault()).toString()
                    )
                }
        }
    })

    findings.forEach { Log.i("A11Y", it.toString()) }
    return findings
}

/** Espera para que terminen de cargar las imágenes antes de la captura; Espresso no sincroniza con Glide. */
private const val SETTLE_MS = 1_500L
