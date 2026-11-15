package co.edu.uniandes.vinilos.models

/**
 * Premio que ganó un artista (`performerPrizes` de `GET /musicians/{id}` y `GET /bands/{id}`).
 * El servidor solo envía [id] y [premiationDate]; [name] y [organization] vienen de `GET /prizes`
 * y quedan en null si esa consulta falla o el premio no aparece.
 */
data class ArtistPrize(
    val id: Int,
    /** Fecha ISO 8601 del servidor, por ejemplo `1978-12-10T00:00:00.000Z`. */
    val premiationDate: String,
    val name: String? = null,
    val organization: String? = null
)
