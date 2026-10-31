package co.edu.uniandes.vinilos.models

/**
 * Opinión de un coleccionista sobre un álbum, con calificación de 1 a 5.
 * [author] es el nombre del coleccionista: `GET /albums/{id}` no lo trae, así que el repositorio lo
 * completa a partir de `GET /collectors`; queda en null si no se pudo obtener.
 */
data class Comment(
    val id: Int,
    val description: String,
    val rating: Int,
    val author: String? = null
)
