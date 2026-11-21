package co.edu.uniandes.vinilos.repositories

import co.edu.uniandes.vinilos.fakes.FakeCollectorService
import co.edu.uniandes.vinilos.fakes.FakeAlbumService as RecordingAlbumService
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.models.Comment
import co.edu.uniandes.vinilos.models.NewAlbum
import co.edu.uniandes.vinilos.network.AlbumService
import com.android.volley.VolleyError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumRepositoryTest {

    private class FakeAlbumService(private val album: Album) : AlbumService {
        override suspend fun getAlbums(): List<Album> = listOf(album)
        override suspend fun getAlbum(id: Int): Album = album
        override suspend fun createAlbum(album: NewAlbum): Album = this.album
    }

    private val album = Album(
        id = 100, name = "Buscando América", cover = "", releaseDate = "1984-08-01T00:00:00.000Z",
        description = "", genre = "Salsa", recordLabel = "Elektra", performers = emptyList(), tracks = emptyList(),
        comments = listOf(Comment(100, "El más relevante", 5), Comment(200, "Sin autor conocido", 3))
    )

    private val manolo = Collector(100, "Manolo Bellon", "", "", listOf(Comment(100, "El más relevante", 5)), 0)

    @Test
    fun getAlbum_addsTheCollectorNameAsCommentAuthor() = runTest {
        val repository = AlbumRepository(FakeAlbumService(album), FakeCollectorService { listOf(manolo) })

        val comments = repository.getAlbum(100).comments

        assertEquals("Manolo Bellon", comments[0].author)
        assertNull(comments[1].author)
    }

    @Test
    fun getAlbum_returnsCommentsWithoutAuthorWhenCollectorsFail() = runTest {
        val repository = AlbumRepository(FakeAlbumService(album), FakeCollectorService { throw VolleyError("sin red") })

        val result = repository.getAlbum(100)

        assertEquals(album.comments.map { it.description }, result.comments.map { it.description })
        result.comments.forEach { assertNull(it.author) }
    }

    @Test
    fun commentAuthors_mapsEachCommentIdToItsCollector() {
        val jaime = Collector(101, "Jaime Monsalve", "", "", listOf(Comment(101, "", 5), Comment(102, "", 4)), 0)

        val authors = AlbumRepository.commentAuthors(listOf(manolo, jaime))

        assertEquals(mapOf(100 to "Manolo Bellon", 101 to "Jaime Monsalve", 102 to "Jaime Monsalve"), authors)
    }

    private val newAlbum = NewAlbum("Siembra", "https://portada/siembra.jpg", "1978-09-01", "Salsa", "Salsa", "Fania Records")

    @Test
    fun createAlbum_sendsTheAlbumAndReturnsTheOneCreatedByTheServer() = runTest {
        val service = RecordingAlbumService().apply { created = { RecordingAlbumService.album(150, it.name) } }
        val repository = AlbumRepository(service, FakeCollectorService())

        val created = repository.createAlbum(newAlbum)

        assertEquals(listOf(newAlbum), service.createRequests)
        assertEquals(150, created.id)
        assertEquals("Siembra", created.name)
    }

    @Test
    fun createAlbum_propagatesTheServerError() = runTest {
        val service = RecordingAlbumService().apply { created = { throw VolleyError("400 Bad Request") } }
        val repository = AlbumRepository(service, FakeCollectorService())

        val result = runCatching { repository.createAlbum(newAlbum) }

        assertTrue(result.exceptionOrNull() is VolleyError)
    }
}
