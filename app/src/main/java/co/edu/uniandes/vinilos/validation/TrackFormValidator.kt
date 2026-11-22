package co.edu.uniandes.vinilos.validation

import androidx.annotation.StringRes
import co.edu.uniandes.vinilos.R

/** Datos de la hoja "Agregar track" (HU08) tal como los escribió el usuario. */
data class TrackForm(
    val name: String = "",
    val duration: String = ""
)

/** Campos de la hoja "Agregar track"; la vista muestra cada error debajo de su campo. */
enum class TrackField { NAME, DURATION }

/** Errores de validación de la hoja "Agregar track", con el mensaje que la vista muestra debajo del campo. */
enum class TrackFormError(@StringRes val message: Int) {
    NAME_REQUIRED(R.string.add_track_error_name_required),
    NAME_TOO_LONG(R.string.add_track_error_name_too_long),
    DURATION_REQUIRED(R.string.add_track_error_duration_required),
    DURATION_FORMAT(R.string.add_track_error_duration_format),
    DURATION_SECONDS(R.string.add_track_error_duration_seconds)
}

/**
 * Reglas de validación de la hoja "Agregar track" (HU08). No depende de la vista: recibe lo que escribió el usuario y
 * devuelve un error por cada campo inválido, para que el ViewModel decida si envía `POST /albums/{id}/tracks`.
 */
class TrackFormValidator {

    /** Errores del formulario por campo; si está vacío, el formulario es válido y se puede enviar. */
    fun validate(form: TrackForm): Map<TrackField, TrackFormError> = buildMap {
        nameError(form.name)?.let { put(TrackField.NAME, it) }
        durationError(form.duration)?.let { put(TrackField.DURATION, it) }
    }

    fun nameError(name: String): TrackFormError? = when {
        name.isBlank() -> TrackFormError.NAME_REQUIRED
        name.trim().length > NAME_MAX_LENGTH -> TrackFormError.NAME_TOO_LONG
        else -> null
    }

    /**
     * La duración va como `mm:ss`: uno o dos dígitos de minutos (BackVynils guarda, por ejemplo, "5:05") y siempre
     * dos de segundos, entre 00 y 59.
     */
    fun durationError(duration: String): TrackFormError? {
        val text = duration.trim()
        if (text.isEmpty()) return TrackFormError.DURATION_REQUIRED
        val match = DURATION_PATTERN.matchEntire(text) ?: return TrackFormError.DURATION_FORMAT
        val seconds = match.groupValues[2].toInt()
        return if (seconds > MAX_SECONDS) TrackFormError.DURATION_SECONDS else null
    }

    companion object {
        /** Máximo de caracteres del nombre; debe coincidir con `@integer/track_name_max_length` del contador. */
        const val NAME_MAX_LENGTH = 100

        private const val MAX_SECONDS = 59

        private val DURATION_PATTERN = Regex("""(\d{1,2}):(\d{2})""")
    }
}
