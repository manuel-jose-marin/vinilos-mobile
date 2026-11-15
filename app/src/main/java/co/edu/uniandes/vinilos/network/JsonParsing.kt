package co.edu.uniandes.vinilos.network

import co.edu.uniandes.vinilos.models.Comment
import co.edu.uniandes.vinilos.models.Performer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /** Artista tal como lo devuelve BackVynils dentro de un álbum o como integrante de una banda. */
    fun parsePerformer(json: JSONObject) = Performer(
        id = json.getInt("id"),
        name = json.getString("name"),
        image = json.optString("image"),
        description = json.optString("description")
    )
}

/** Convierte un arreglo JSON de objetos en lista; si el servidor no lo envía (null) devuelve una lista vacía. */
internal fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> =
    if (this == null) emptyList() else List(length()) { transform(getJSONObject(it)) }

/**
 * Convierte la respuesta en modelos fuera del hilo principal. Volley descarga en su propio hilo, pero
 * entrega la respuesta en el hilo principal; recorrer el JSON allí bloquearía la interfaz con listas grandes.
 */
internal suspend fun <J, T> parseOffMainThread(json: J, parse: (J) -> T): T =
    withContext(Dispatchers.Default) { parse(json) }
