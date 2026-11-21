package co.edu.uniandes.vinilos.models

/**
 * Coleccionista de BackVynils (`GET /collectors` y `GET /collectors/{id}`), con los comentarios que ha escrito.
 * [albumCount] es el número de álbumes de su colección (`collectorAlbums`), que muestra el listado (HU05).
 * [favoritePerformers] son sus artistas favoritos, que muestra el detalle (HU06); el listado no los usa.
 */
data class Collector(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
    val comments: List<Comment>,
    val albumCount: Int,
    val favoritePerformers: List<Performer> = emptyList()
)
