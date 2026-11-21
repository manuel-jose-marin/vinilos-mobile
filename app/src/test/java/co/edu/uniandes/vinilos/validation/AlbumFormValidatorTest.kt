package co.edu.uniandes.vinilos.validation

import co.edu.uniandes.vinilos.validation.AlbumFormValidator.Companion.DESCRIPTION_MAX_LENGTH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class AlbumFormValidatorTest {

    private val day = 24L * 60 * 60 * 1000

    /** 2026-11-19 a la medianoche UTC; las pruebas no dependen de la fecha real. */
    private val today = 1_795_046_400_000L

    private val validator = AlbumFormValidator(today = { today })

    private val validForm = AlbumForm(
        name = "Buscando América",
        cover = "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg",
        releaseDate = today - 365 * day,
        description = "Primer álbum de la banda Seis del Solar.",
        genre = "Salsa",
        recordLabel = "Elektra"
    )

    // Formulario completo

    @Test
    fun validate_validForm_hasNoErrors() {
        assertTrue(validator.validate(validForm).isEmpty())
    }

    @Test
    fun validate_emptyForm_reportsEveryRequiredField() {
        val errors = validator.validate(AlbumForm())

        assertEquals(
            mapOf(
                AlbumField.NAME to AlbumFormError.NAME_REQUIRED,
                AlbumField.COVER to AlbumFormError.COVER_REQUIRED,
                AlbumField.RELEASE_DATE to AlbumFormError.RELEASE_DATE_REQUIRED,
                AlbumField.DESCRIPTION to AlbumFormError.DESCRIPTION_REQUIRED,
                AlbumField.GENRE to AlbumFormError.GENRE_REQUIRED,
                AlbumField.RECORD_LABEL to AlbumFormError.RECORD_LABEL_REQUIRED
            ),
            errors
        )
    }

    @Test
    fun validate_onlyTheInvalidFieldIsReported() {
        val errors = validator.validate(validForm.copy(cover = "http://portada.jpg"))

        assertEquals(mapOf(AlbumField.COVER to AlbumFormError.COVER_INVALID), errors)
    }

    // Nombre

    @Test
    fun name_emptyOrOnlySpaces_isRequired() {
        assertEquals(AlbumFormError.NAME_REQUIRED, validator.nameError(""))
        assertEquals(AlbumFormError.NAME_REQUIRED, validator.nameError("   "))
    }

    @Test
    fun name_withText_isValid() {
        assertNull(validator.nameError("A"))
    }

    // Portada

    @Test
    fun cover_emptyOrOnlySpaces_isRequired() {
        assertEquals(AlbumFormError.COVER_REQUIRED, validator.coverError(""))
        assertEquals(AlbumFormError.COVER_REQUIRED, validator.coverError("   "))
    }

    @Test
    fun cover_http_isInvalid() {
        assertEquals(AlbumFormError.COVER_INVALID, validator.coverError("http://portada.jpg"))
    }

    @Test
    fun cover_withoutScheme_isInvalid() {
        assertEquals(AlbumFormError.COVER_INVALID, validator.coverError("portada.jpg"))
    }

    @Test
    fun cover_onlyTheScheme_isInvalid() {
        assertEquals(AlbumFormError.COVER_INVALID, validator.coverError("https://"))
    }

    @Test
    fun cover_withSpacesInside_isInvalid() {
        assertEquals(AlbumFormError.COVER_INVALID, validator.coverError("https://mi portada.jpg"))
    }

    @Test
    fun cover_https_isValid() {
        assertNull(validator.coverError("https://portada.jpg"))
        assertNull(validator.coverError("HTTPS://portada.jpg"))
        assertNull(validator.coverError("  https://portada.jpg  "))
    }

    // Fecha de lanzamiento

    @Test
    fun releaseDate_notChosen_isRequired() {
        assertEquals(AlbumFormError.RELEASE_DATE_REQUIRED, validator.releaseDateError(null))
    }

    @Test
    fun releaseDate_yesterday_isValid() {
        assertNull(validator.releaseDateError(today - day))
    }

    @Test
    fun releaseDate_today_isValid() {
        assertNull(validator.releaseDateError(today))
    }

    @Test
    fun releaseDate_tomorrow_isFuture() {
        assertEquals(AlbumFormError.RELEASE_DATE_FUTURE, validator.releaseDateError(today + day))
    }

    @Test
    fun todayAsUtcMidnight_isTheLocalDateAtUtcMidnight() {
        val local = Calendar.getInstance()
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = AlbumFormValidator.todayAsUtcMidnight()
        }

        assertEquals(local.get(Calendar.YEAR), utc.get(Calendar.YEAR))
        assertEquals(local.get(Calendar.DAY_OF_YEAR), utc.get(Calendar.DAY_OF_YEAR))
        assertEquals(0L, utc.timeInMillis % day)
    }

    // Descripción

    @Test
    fun description_emptyOrOnlySpaces_isRequired() {
        assertEquals(AlbumFormError.DESCRIPTION_REQUIRED, validator.descriptionError(""))
        assertEquals(AlbumFormError.DESCRIPTION_REQUIRED, validator.descriptionError("   "))
    }

    @Test
    fun description_oneBelowTheLimit_isValid() {
        assertNull(validator.descriptionError("a".repeat(DESCRIPTION_MAX_LENGTH - 1)))
    }

    @Test
    fun description_atTheLimit_isValid() {
        assertNull(validator.descriptionError("a".repeat(DESCRIPTION_MAX_LENGTH)))
    }

    @Test
    fun description_oneAboveTheLimit_isTooLong() {
        assertEquals(
            AlbumFormError.DESCRIPTION_TOO_LONG,
            validator.descriptionError("a".repeat(DESCRIPTION_MAX_LENGTH + 1))
        )
    }

    // Género y sello

    @Test
    fun genre_notChosen_isRequired() {
        assertEquals(AlbumFormError.GENRE_REQUIRED, validator.genreError(null))
    }

    @Test
    fun genre_valueTheApiDoesNotAccept_isRequired() {
        assertEquals(AlbumFormError.GENRE_REQUIRED, validator.genreError("Reggaeton"))
    }

    @Test
    fun genre_everyApiValue_isValid() {
        AlbumFormValidator.GENRES.forEach { assertNull(validator.genreError(it)) }
    }

    @Test
    fun recordLabel_notChosen_isRequired() {
        assertEquals(AlbumFormError.RECORD_LABEL_REQUIRED, validator.recordLabelError(null))
    }

    @Test
    fun recordLabel_valueTheApiDoesNotAccept_isRequired() {
        assertEquals(AlbumFormError.RECORD_LABEL_REQUIRED, validator.recordLabelError("Universal"))
    }

    @Test
    fun recordLabel_everyApiValue_isValid() {
        AlbumFormValidator.RECORD_LABELS.forEach { assertNull(validator.recordLabelError(it)) }
    }
}
