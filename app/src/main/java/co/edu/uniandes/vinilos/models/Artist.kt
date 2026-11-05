package co.edu.uniandes.vinilos.models

/** Tipo de artista: cada uno tiene su propio endpoint en BackVynils. */
enum class ArtistType { MUSICIAN, BAND }

/** Músico o banda de BackVynils (`GET /musicians` y `GET /bands`). */
data class Artist(
    val id: Int,
    val name: String,
    val image: String,
    val description: String,
    val type: ArtistType,
    /** Fecha ISO 8601 del servidor: nacimiento de un músico o fundación de una banda. */
    val date: String,
    val albums: List<Album>,
    /** Integrantes de una banda; vacío para los músicos. */
    val members: List<Performer>
)
