package co.edu.uniandes.vinilos.network

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkServiceAdapterTest {

    @Test
    fun buildUrl_joinsBaseAndPathWithSingleSlash() {
        val expected = "http://10.0.2.2:3000/albums/1"

        assertEquals(expected, NetworkServiceAdapter.buildUrl("http://10.0.2.2:3000/", "albums/1"))
        assertEquals(expected, NetworkServiceAdapter.buildUrl("http://10.0.2.2:3000", "albums/1"))
        assertEquals(expected, NetworkServiceAdapter.buildUrl("http://10.0.2.2:3000/", "/albums/1"))
        assertEquals(expected, NetworkServiceAdapter.buildUrl("http://10.0.2.2:3000", "/albums/1"))
    }
}
