package co.edu.uniandes.vinilos.utils

import co.edu.uniandes.vinilos.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Llamadas directas a BackVynils para que las pruebas E2E preparen sus propios datos y los borren al terminar,
 * sin depender de lo que dejen otras pruebas. Corren en el hilo de la instrumentación, no en el principal.
 */
object BackVynilsApi {

    /** Crea un álbum con [name] y devuelve su id. */
    fun createAlbum(name: String): Int {
        val body = JSONObject()
            .put("name", name)
            .put("cover", "https://i.pinimg.com/564x/aa/5f/ed/aa5fed7fac61cc8f41d1e79db917a7cd.jpg")
            .put("releaseDate", "2020-01-01")
            .put("description", "Álbum creado por una prueba E2E")
            .put("genre", "Rock")
            .put("recordLabel", "EMI")
        return JSONObject(request("POST", "albums", body.toString(), expectedCode = 200)).getInt("id")
    }

    /** Tracks del álbum [albumId] tal como los guarda el servidor. */
    fun tracks(albumId: Int): JSONArray = JSONArray(request("GET", "albums/$albumId/tracks", expectedCode = 200))

    /** Borra los tracks del álbum [albumId] y después el álbum, que el servidor no deja borrar si tiene tracks. */
    fun deleteAlbum(albumId: Int) {
        val albumTracks = tracks(albumId)
        for (i in 0 until albumTracks.length()) {
            request("DELETE", "albums/$albumId/tracks/${albumTracks.getJSONObject(i).getInt("id")}", expectedCode = 204)
        }
        request("DELETE", "albums/$albumId", expectedCode = 204)
    }

    private fun request(method: String, path: String, body: String? = null, expectedCode: Int): String {
        val connection = URL("${BuildConfig.BASE_URL}$path").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = NETWORK_TIMEOUT_MS.toInt()
            connection.readTimeout = NETWORK_TIMEOUT_MS.toInt()
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toByteArray()) }
            }
            check(connection.responseCode == expectedCode) {
                "$method $path respondió ${connection.responseCode}, se esperaba $expectedCode"
            }
            if (expectedCode == 204) "" else connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
