package co.edu.uniandes.vinilos.models

/** Opinión de un coleccionista sobre un álbum, con calificación numérica. */
data class Comment(
    val id: Int,
    val description: String,
    val rating: Int
)
