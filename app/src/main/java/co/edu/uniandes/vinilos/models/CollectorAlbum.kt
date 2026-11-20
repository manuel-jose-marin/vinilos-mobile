package co.edu.uniandes.vinilos.models

/**
 * Álbum de la colección de un coleccionista (`GET /collectors/{id}/albums`), con el precio al que lo ofrece y su
 * estado. El servidor no trae el artista del álbum: el repositorio lo completa en [album] a partir de `GET /albums`.
 */
data class CollectorAlbum(
    val id: Int,
    /** Precio en pesos colombianos (COP). */
    val price: Int,
    /** `Active` o `Inactive`, tal como lo devuelve BackVynils. */
    val status: String,
    val album: Album
) {
    /** Si el álbum está disponible para venta o intercambio; la vista muestra "Disponible" o "No disponible". */
    val isAvailable: Boolean get() = status == STATUS_ACTIVE

    companion object {
        const val STATUS_ACTIVE = "Active"
        const val STATUS_INACTIVE = "Inactive"
    }
}
