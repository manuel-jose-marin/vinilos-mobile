package co.edu.uniandes.vinilos.ui

import java.util.Locale

/** Textos formateados para el listado de coleccionistas (HU05). */
object CollectorText {

    /**
     * Iniciales para el avatar: la primera letra de las dos primeras palabras del nombre, en mayúscula
     * ("Manolo Bellon" → "MB"). Con una sola palabra devuelve una letra y con un nombre vacío, "?".
     */
    fun initials(name: String): String {
        val initials = name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.substring(0, it.offsetByCodePoints(0, 1)) }
        return initials.uppercase(Locale("es", "CO")).ifEmpty { "?" }
    }
}
