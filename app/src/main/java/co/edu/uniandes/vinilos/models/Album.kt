package co.edu.uniandes.vinilos.models

/** Álbum del catálogo tal como lo devuelve BackVynils (`GET /albums` y `GET /albums/{id}`). */
data class Album(
    val id: Int,
    val name: String,
    val cover: String,
    /** Fecha ISO 8601 del servidor, por ejemplo `1984-08-01T00:00:00.000Z`. */
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String,
    val performers: List<Performer>,
    val tracks: List<Track>,
    val comments: List<Comment>
) {
    /** Año de lanzamiento (los cuatro primeros dígitos de [releaseDate]). */
    val releaseYear: String get() = releaseDate.take(4)
}
