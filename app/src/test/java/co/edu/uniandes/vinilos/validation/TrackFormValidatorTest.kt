package co.edu.uniandes.vinilos.validation

import co.edu.uniandes.vinilos.validation.TrackFormValidator.Companion.NAME_MAX_LENGTH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackFormValidatorTest {

    private val validator = TrackFormValidator()

    private val validForm = TrackForm(name = "Plástico", duration = "06:52")

    // Formulario completo

    @Test
    fun validate_aValidForm_hasNoErrors() {
        assertTrue(validator.validate(validForm).isEmpty())
    }

    @Test
    fun validate_anEmptyForm_marksBothFields() {
        val errors = validator.validate(TrackForm())

        assertEquals(
            mapOf(TrackField.NAME to TrackFormError.NAME_REQUIRED, TrackField.DURATION to TrackFormError.DURATION_REQUIRED),
            errors
        )
    }

    // Nombre (criterios 3 y 4)

    @Test
    fun nameError_emptyOrOnlySpaces_isRequired() {
        assertEquals(TrackFormError.NAME_REQUIRED, validator.nameError(""))
        assertEquals(TrackFormError.NAME_REQUIRED, validator.nameError("   "))
    }

    @Test
    fun nameError_upToTheMaximumLength_isValid() {
        assertNull(validator.nameError("a".repeat(NAME_MAX_LENGTH - 1)))
        assertNull(validator.nameError("a".repeat(NAME_MAX_LENGTH)))
    }

    @Test
    fun nameError_overTheMaximumLength_isTooLong() {
        assertEquals(TrackFormError.NAME_TOO_LONG, validator.nameError("a".repeat(NAME_MAX_LENGTH + 1)))
    }

    @Test
    fun nameError_spacesAroundDoNotCountForTheLength() {
        assertNull(validator.nameError("  " + "a".repeat(NAME_MAX_LENGTH) + "  "))
    }

    @Test
    fun nameMaxLength_isOneHundred() {
        assertEquals(100, NAME_MAX_LENGTH)
    }

    // Duración (criterios 3, 5 y 6)

    @Test
    fun durationError_emptyOrOnlySpaces_isRequired() {
        assertEquals(TrackFormError.DURATION_REQUIRED, validator.durationError(""))
        assertEquals(TrackFormError.DURATION_REQUIRED, validator.durationError("  "))
    }

    @Test
    fun durationError_withoutTheMmSsFormat_isAFormatError() {
        listOf("6.31", "abc", "4:5", "0415", "04:", ":15", "123:00", "04:15:00", "-4:15", "04 15").forEach {
            assertEquals("Duración \"$it\"", TrackFormError.DURATION_FORMAT, validator.durationError(it))
        }
    }

    @Test
    fun durationError_withMmSsFormat_isValid() {
        listOf("04:15", "4:15", "5:05", "00:01", "99:59", " 06:52 ").forEach {
            assertNull("Duración \"$it\"", validator.durationError(it))
        }
    }

    @Test
    fun durationError_secondsUpTo59_isValid() {
        assertNull(validator.durationError("06:59"))
        assertNull(validator.durationError("06:00"))
    }

    @Test
    fun durationError_secondsOver59_isASecondsError() {
        assertEquals(TrackFormError.DURATION_SECONDS, validator.durationError("06:60"))
        assertEquals(TrackFormError.DURATION_SECONDS, validator.durationError("06:75"))
        assertEquals(TrackFormError.DURATION_SECONDS, validator.durationError("06:99"))
    }
}
