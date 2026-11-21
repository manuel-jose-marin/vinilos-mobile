package co.edu.uniandes.vinilos.validation

import androidx.annotation.StringRes
import co.edu.uniandes.vinilos.R
import java.util.Calendar
import java.util.TimeZone

/**
 * Datos del formulario de creación de álbum (HU07) tal como los escribió el usuario.
 * [releaseDate] es la fecha elegida en el selector, en milisegundos a la medianoche UTC de ese día (la convención
 * de `MaterialDatePicker`); es null mientras el usuario no elija una.
 */
data class AlbumForm(
    val name: String = "",
    val cover: String = "",
    val releaseDate: Long? = null,
    val description: String = "",
    val genre: String? = null,
    val recordLabel: String? = null
)

/** Campos del formulario de álbum; la vista muestra cada error debajo de su campo. */
enum class AlbumField { NAME, COVER, RELEASE_DATE, DESCRIPTION, GENRE, RECORD_LABEL }

/** Errores de validación del formulario de álbum, con el mensaje que la vista muestra debajo del campo. */
enum class AlbumFormError(@StringRes val message: Int) {
    NAME_REQUIRED(R.string.album_form_error_name_required),
    COVER_REQUIRED(R.string.album_form_error_cover_required),
    COVER_INVALID(R.string.album_form_error_cover_invalid),
    RELEASE_DATE_REQUIRED(R.string.album_form_error_release_date_required),
    RELEASE_DATE_FUTURE(R.string.album_form_error_release_date_future),
    DESCRIPTION_REQUIRED(R.string.album_form_error_description_required),
    DESCRIPTION_TOO_LONG(R.string.album_form_error_description_too_long),
    GENRE_REQUIRED(R.string.album_form_error_genre_required),
    RECORD_LABEL_REQUIRED(R.string.album_form_error_record_label_required)
}

/**
 * Reglas de validación del formulario de creación de álbum (HU07). No depende de la vista: recibe lo que escribió
 * el usuario y devuelve un error por cada campo inválido, para que el ViewModel decida si envía `POST /albums`.
 *
 * @param today día actual en milisegundos a la medianoche UTC; se inyecta para poder probar la regla de la fecha.
 */
class AlbumFormValidator(private val today: () -> Long = { todayAsUtcMidnight() }) {

    /** Errores del formulario por campo; si está vacío, el formulario es válido y se puede enviar. */
    fun validate(form: AlbumForm): Map<AlbumField, AlbumFormError> = buildMap {
        nameError(form.name)?.let { put(AlbumField.NAME, it) }
        coverError(form.cover)?.let { put(AlbumField.COVER, it) }
        releaseDateError(form.releaseDate)?.let { put(AlbumField.RELEASE_DATE, it) }
        descriptionError(form.description)?.let { put(AlbumField.DESCRIPTION, it) }
        genreError(form.genre)?.let { put(AlbumField.GENRE, it) }
        recordLabelError(form.recordLabel)?.let { put(AlbumField.RECORD_LABEL, it) }
    }

    fun nameError(name: String): AlbumFormError? =
        if (name.isBlank()) AlbumFormError.NAME_REQUIRED else null

    /** La portada debe ser una URL `https://` con algo después del esquema y sin espacios. */
    fun coverError(cover: String): AlbumFormError? {
        val url = cover.trim()
        return when {
            url.isEmpty() -> AlbumFormError.COVER_REQUIRED
            !url.startsWith(HTTPS_PREFIX, ignoreCase = true) ||
                url.length == HTTPS_PREFIX.length ||
                url.any { it.isWhitespace() } -> AlbumFormError.COVER_INVALID
            else -> null
        }
    }

    /** La fecha es obligatoria y puede ser hoy, pero no un día futuro. */
    fun releaseDateError(releaseDate: Long?): AlbumFormError? = when {
        releaseDate == null -> AlbumFormError.RELEASE_DATE_REQUIRED
        releaseDate > today() -> AlbumFormError.RELEASE_DATE_FUTURE
        else -> null
    }

    fun descriptionError(description: String): AlbumFormError? = when {
        description.isBlank() -> AlbumFormError.DESCRIPTION_REQUIRED
        description.trim().length > DESCRIPTION_MAX_LENGTH -> AlbumFormError.DESCRIPTION_TOO_LONG
        else -> null
    }

    /** Solo se aceptan los géneros que admite BackVynils. */
    fun genreError(genre: String?): AlbumFormError? =
        if (genre !in GENRES) AlbumFormError.GENRE_REQUIRED else null

    /** Solo se aceptan los sellos que admite BackVynils. */
    fun recordLabelError(recordLabel: String?): AlbumFormError? =
        if (recordLabel !in RECORD_LABELS) AlbumFormError.RECORD_LABEL_REQUIRED else null

    companion object {
        /** Máximo de caracteres de la descripción; la vista lo usa también para el contador y el `maxLength`. */
        const val DESCRIPTION_MAX_LENGTH = 300

        /** Géneros que acepta `POST /albums`, en el orden en que los muestra el desplegable. */
        val GENRES = listOf("Classical", "Salsa", "Rock", "Folk")

        /** Sellos discográficos que acepta `POST /albums`, en el orden en que los muestra el desplegable. */
        val RECORD_LABELS = listOf("Sony Music", "EMI", "Discos Fuentes", "Elektra", "Fania Records")

        private const val HTTPS_PREFIX = "https://"

        /** Día de hoy en la zona horaria del teléfono, expresado como medianoche UTC (igual que el selector de fecha). */
        fun todayAsUtcMidnight(): Long {
            val local = Calendar.getInstance()
            return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
            }.timeInMillis
        }
    }
}
