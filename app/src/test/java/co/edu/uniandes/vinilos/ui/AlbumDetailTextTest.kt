package co.edu.uniandes.vinilos.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumDetailTextTest {

    @Test
    fun releaseDate_formatsTheServerDateInSpanish() {
        assertEquals("1 de agosto de 1984", AlbumDetailText.releaseDate("1984-08-01T00:00:00.000Z"))
    }

    @Test
    fun releaseDate_returnsTheOriginalTextWhenItIsNotADate() {
        assertEquals("sin fecha", AlbumDetailText.releaseDate("sin fecha"))
        assertEquals("", AlbumDetailText.releaseDate(""))
    }
}
