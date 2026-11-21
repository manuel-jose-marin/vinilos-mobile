package co.edu.uniandes.vinilos.models

import org.json.JSONObject

/** Datos para crear un nuevo álbum (`POST /albums`) en BackVynils (HU07). */
data class NewAlbum(
    val name: String,
    val cover: String,
    val releaseDate: String,
    val description: String,
    val genre: String,
    val recordLabel: String
) {
    /** Convierte el objeto a JSON para enviarlo en el cuerpo del POST. */
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("cover", cover)
        put("releaseDate", releaseDate)
        put("description", description)
        put("genre", genre)
        put("recordLabel", recordLabel)
    }
}
