package co.edu.uniandes.vinilos.network

import android.content.Context
import co.edu.uniandes.vinilos.BuildConfig
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service Adapter: único punto de acceso a la API REST de BackVynils.
 *
 * Los servicios de cada recurso (álbumes, artistas, coleccionistas) usan estos métodos con la ruta
 * relativa del endpoint, por ejemplo `getArray("albums")`. Los errores de red o del servidor se
 * propagan como [com.android.volley.VolleyError] para que el repositorio o el ViewModel los manejen.
 */
class NetworkServiceAdapter private constructor(context: Context) {

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    /** GET de un endpoint que responde una lista JSON. */
    suspend fun getArray(path: String): JSONArray = enqueue { onSuccess, onError ->
        JsonArrayRequest(Request.Method.GET, url(path), null, onSuccess, onError)
    }

    /** GET de un endpoint que responde un objeto JSON. */
    suspend fun getObject(path: String): JSONObject = enqueue { onSuccess, onError ->
        JsonObjectRequest(Request.Method.GET, url(path), null, onSuccess, onError)
    }

    /** POST de un objeto JSON; devuelve el objeto creado que responde el servidor. */
    suspend fun post(path: String, body: JSONObject): JSONObject = enqueue { onSuccess, onError ->
        JsonObjectRequest(Request.Method.POST, url(path), body, onSuccess, onError)
    }

    private fun url(path: String): String = buildUrl(BuildConfig.BASE_URL, path)

    /** Encola la petición en Volley y suspende hasta su respuesta; si se cancela la corrutina, cancela la petición. */
    private suspend fun <T> enqueue(
        createRequest: (Response.Listener<T>, Response.ErrorListener) -> Request<T>
    ): T = suspendCancellableCoroutine { continuation ->
        val request = createRequest(
            { response -> continuation.resume(response) },
            { error -> continuation.resumeWithException(error) }
        )
        request.retryPolicy = DefaultRetryPolicy(TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        requestQueue.add(request)
        continuation.invokeOnCancellation { request.cancel() }
    }

    companion object {
        /**
         * La API desplegada en Render se duerme sin tráfico y tarda hasta un minuto en despertar;
         * con el timeout por defecto de Volley (2,5 s) la primera carga fallaría.
         */
        private const val TIMEOUT_MS = 30_000
        private const val MAX_RETRIES = 1

        @Volatile
        private var instance: NetworkServiceAdapter? = null

        fun getInstance(context: Context): NetworkServiceAdapter =
            instance ?: synchronized(this) {
                instance ?: NetworkServiceAdapter(context).also { instance = it }
            }

        /** Une la URL base y la ruta del endpoint con una sola barra entre ellas. */
        fun buildUrl(baseUrl: String, path: String): String =
            baseUrl.trimEnd('/') + "/" + path.trimStart('/')
    }
}
