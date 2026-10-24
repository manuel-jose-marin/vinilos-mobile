package co.edu.uniandes.vinilos.models

/** Artista de un álbum: puede ser un músico o una banda. */
data class Performer(
    val id: Int,
    val name: String,
    val image: String,
    val description: String
)
