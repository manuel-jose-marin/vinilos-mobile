package co.edu.uniandes.vinilos.models

/**
 * Coleccionista de BackVynils (`GET /collectors`), con los comentarios que ha escrito.
 * [albumCount] es el número de álbumes de su colección (`collectorAlbums`), que muestra el listado (HU05).
 */
data class Collector(
    val id: Int,
    val name: String,
    val telephone: String,
    val email: String,
    val comments: List<Comment>,
    val albumCount: Int
)
