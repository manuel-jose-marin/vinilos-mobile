package co.edu.uniandes.vinilos.repositories

import co.edu.uniandes.vinilos.fakes.FakeAlbumService
import co.edu.uniandes.vinilos.fakes.FakeAlbumService.Companion.album
import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.models.CollectorAlbum
import co.edu.uniandes.vinilos.models.Comment
import co.edu.uniandes.vinilos.models.Performer
import com.android.volley.VolleyError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectorRepositoryTest {

    private val ruben = Performer(100, "Rubén Blades Bellido de Luna", "https://ruben.jpg", "")

    private val manolo = Collector(
        id = 100, name = "Manolo Bellon", telephone = "3502457896", email = "manollo@caracol.com.co",
        comments = listOf(Comment(100, "El más relevante", 5)), albumCount = 1, favoritePerformers = listOf(ruben)
    )

    /** La colección como la devuelve `GET /collectors/{id}/albums`: el álbum llega sin artistas. */
    private val buscandoAmerica = CollectorAlbum(100, 35, CollectorAlbum.STATUS_ACTIVE, album(100, "Buscando América"))

    private fun repository(
        collectors: FakeCollectorService = FakeCollectorService(
            result = { listOf(manolo) },
            collector = { manolo },
            collectorAlbums = { listOf(buscandoAmerica) }
        ),
        albums: FakeAlbumService = FakeAlbumService { listOf(album(100, "Buscando América", listOf(ruben))) }
    ) = CollectorRepository(collectors, albums)

    // Listado (HU05)

    @Test
    fun getCollectors_returnsTheListFromService() = runTest {
        assertEquals(listOf(manolo), repository().getCollectors())
    }

    @Test(expected = VolleyError::class)
    fun getCollectors_propagatesExceptionsFromTheService() = runTest {
        repository(FakeCollectorService(result = { throw VolleyError("Error de red") })).getCollectors()
    }

    // Detalle (HU06)

    @Test
    fun getCollectorDetail_requestsTheGivenCollector() = runTest {
        val collectors = FakeCollectorService(collector = { manolo })

        repository(collectors).getCollectorDetail(100)

        assertEquals(listOf(100), collectors.detailRequests)
    }

    @Test
    fun getCollectorDetail_returnsTheCollectorWithFavoritesAndComments() = runTest {
        val detail = repository().getCollectorDetail(100)

        assertEquals(manolo, detail.collector)
        assertEquals(listOf(ruben), detail.collector.favoritePerformers)
        assertEquals("El más relevante", detail.collector.comments.single().description)
    }

    @Test
    fun getCollectorDetail_returnsTheCollectionWithPriceAndStatus() = runTest {
        val albums = repository().getCollectorDetail(100).albums

        assertEquals(1, albums.size)
        assertEquals("Buscando América", albums[0].album.name)
        assertEquals(35, albums[0].price)
        assertTrue(albums[0].isAvailable)
    }

    @Test
    fun getCollectorDetail_addsTheArtistOfEachAlbumFromTheCatalog() = runTest {
        val albums = repository().getCollectorDetail(100).albums

        assertEquals(listOf(ruben), albums[0].album.performers)
    }

    @Test
    fun getCollectorDetail_albumMissingFromTheCatalog_hasNoArtist() = runTest {
        val albums = repository(albums = FakeAlbumService { listOf(album(999, "Otro álbum", listOf(ruben))) })
            .getCollectorDetail(100).albums

        assertTrue(albums[0].album.performers.isEmpty())
    }

    @Test
    fun getCollectorDetail_whenTheCatalogFails_returnsTheCollectionWithoutArtists() = runTest {
        val detail = repository(albums = FakeAlbumService { throw VolleyError("sin red") }).getCollectorDetail(100)

        assertEquals(manolo, detail.collector)
        assertEquals("Buscando América", detail.albums.single().album.name)
        assertTrue(detail.albums.single().album.performers.isEmpty())
    }

    @Test
    fun getCollectorDetail_withoutFavoritesAlbumsOrComments_returnsEmptyLists() = runTest {
        val alone = Collector(101, "Jaime Monsalve", "", "", emptyList(), 0)
        val detail = repository(FakeCollectorService(collector = { alone })).getCollectorDetail(101)

        assertTrue(detail.collector.favoritePerformers.isEmpty())
        assertTrue(detail.collector.comments.isEmpty())
        assertTrue(detail.albums.isEmpty())
    }

    @Test(expected = VolleyError::class)
    fun getCollectorDetail_whenTheCollectorFails_propagatesTheException() = runTest {
        repository(FakeCollectorService(collector = { throw VolleyError("sin red") })).getCollectorDetail(100)
    }

    @Test(expected = VolleyError::class)
    fun getCollectorDetail_whenTheCollectionFails_propagatesTheException() = runTest {
        val collectors = FakeCollectorService(collector = { manolo }, collectorAlbums = { throw VolleyError("sin red") })

        repository(collectors).getCollectorDetail(100)
    }

    @Test
    fun albumPerformers_mapsEachAlbumIdToItsPerformers() {
        val queen = Performer(101, "Queen", "", "")

        val performers = CollectorRepository.albumPerformers(
            listOf(album(100, "Buscando América", listOf(ruben)), album(101, "A Night at the Opera", listOf(queen)))
        )

        assertEquals(mapOf(100 to listOf(ruben), 101 to listOf(queen)), performers)
    }
}
