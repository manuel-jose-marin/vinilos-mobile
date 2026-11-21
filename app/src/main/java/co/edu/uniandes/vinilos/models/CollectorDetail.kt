package co.edu.uniandes.vinilos.models

/** Todo lo que muestra el detalle de un coleccionista (HU06): sus datos, favoritos, comentarios y colección. */
data class CollectorDetail(
    val collector: Collector,
    val albums: List<CollectorAlbum>
)
