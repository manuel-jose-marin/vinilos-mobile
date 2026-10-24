package co.edu.uniandes.vinilos.models

/** Canción de un álbum; [duration] viene como texto `mm:ss`. */
data class Track(
    val id: Int,
    val name: String,
    val duration: String
)
