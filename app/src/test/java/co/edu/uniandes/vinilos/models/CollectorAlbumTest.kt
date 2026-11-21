package co.edu.uniandes.vinilos.models

import co.edu.uniandes.vinilos.fakes.FakeAlbumService.Companion.album
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectorAlbumTest {

    private fun collectorAlbum(status: String) = CollectorAlbum(1, 35, status, album(100, "Buscando América"))

    @Test
    fun isAvailable_active_isTrue() {
        assertTrue(collectorAlbum(CollectorAlbum.STATUS_ACTIVE).isAvailable)
    }

    @Test
    fun isAvailable_inactive_isFalse() {
        assertFalse(collectorAlbum(CollectorAlbum.STATUS_INACTIVE).isAvailable)
    }
}
