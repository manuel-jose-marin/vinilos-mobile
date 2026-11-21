package co.edu.uniandes.vinilos.models

import org.json.JSONObject

/** Datos para asociar un nuevo track a un álbum (`POST /albums/{id}/tracks`) en BackVynils (HU08). */
data class NewTrack(
    val name: String,
    val duration: String
) {
    /** Convierte el objeto a JSON para enviarlo en el cuerpo del POST. */
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("duration", duration)
    }
}
