package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Comment
import org.json.JSONArray
import org.json.JSONObject

/** Utilidades de parseo compartidas por los servicios REST. */
internal object JsonParsing {

    /** Comentario tal como lo devuelve BackVynils dentro de un álbum o de un coleccionista (sin autor). */
    fun parseComment(json: JSONObject) = Comment(
        id = json.getInt("id"),
        description = json.optString("description"),
        rating = json.optInt("rating")
    )
}

/** Convierte un arreglo JSON de objetos en lista; si el servidor no lo envía (null) devuelve una lista vacía. */
internal fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> =
    if (this == null) emptyList() else List(length()) { transform(getJSONObject(it)) }
