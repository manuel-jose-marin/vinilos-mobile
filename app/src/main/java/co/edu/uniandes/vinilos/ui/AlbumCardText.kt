package co.edu.uniandes.vinilos.ui

import co.edu.uniandes.vinilos.models.Album

/** Textos de la tarjeta de un álbum en el listado (HU01). */
object AlbumCardText {

    /** Nombres de los artistas separados por coma; vacío si el álbum no tiene artistas. */
    fun artists(album: Album): String = album.performers.joinToString(", ") { it.name }

    /** "Año · Género", por ejemplo "1984 · Salsa". Omite el dato que falte. */
    fun yearAndGenre(album: Album): String =
        listOf(album.releaseYear, album.genre).filter { it.isNotBlank() }.joinToString(" · ")
}
