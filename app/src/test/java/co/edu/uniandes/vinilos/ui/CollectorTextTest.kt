package co.edu.uniandes.vinilos.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectorTextTest {

    @Test
    fun initials_takesTheFirstLetterOfTheFirstTwoWords() {
        assertEquals("MB", CollectorText.initials("Manolo Bellon"))
        assertEquals("JM", CollectorText.initials("Jaime Monsalve Restrepo"))
    }

    @Test
    fun initials_isUppercaseAndIgnoresExtraSpaces() {
        assertEquals("ÁP", CollectorText.initials("  álvaro   pérez "))
    }

    @Test
    fun initials_withASingleWordReturnsOneLetter() {
        assertEquals("M", CollectorText.initials("Manolo"))
    }

    @Test
    fun initials_withABlankNameReturnsAPlaceholder() {
        assertEquals("?", CollectorText.initials("   "))
    }
}
