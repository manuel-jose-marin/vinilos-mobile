package co.edu.uniandes.vinilos.models

/**
 * Artista de un álbum o artista favorito de un coleccionista: puede ser un músico o una banda.
 * [type] solo se conoce en los favoritos de un coleccionista (HU06), donde hace falta para abrir el detalle del
 * artista; en los artistas de un álbum y en los integrantes de una banda queda en null.
 */
data class Performer(
    val id: Int,
    val name: String,
    val image: String,
    val description: String,
    val type: ArtistType? = null
)
