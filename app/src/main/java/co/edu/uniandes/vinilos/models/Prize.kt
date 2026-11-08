package co.edu.uniandes.vinilos.models

/**
 * Premio de BackVynils (`GET /prizes`). [performerPrizeIds] son los ids de cada vez que se otorgó a un
 * artista; sirven para completar el nombre y la organización de cada [ArtistPrize].
 */
data class Prize(
    val id: Int,
    val name: String,
    val organization: String,
    val performerPrizeIds: List<Int>
)
