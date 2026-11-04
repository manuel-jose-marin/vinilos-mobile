package co.edu.uniandes.vinilos.ui

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Textos de la pantalla del detalle de un álbum (HU02). */
object AlbumDetailText {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    /**
     * Formateadores creados una sola vez y no en cada llamada. `SimpleDateFormat` no es seguro entre
     * hilos; se usan solo desde el hilo principal, al mostrar el detalle.
     */
    private val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utc; isLenient = false }
    private val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "CO")).apply { timeZone = utc }

    /**
     * Fecha de lanzamiento legible, por ejemplo "1 de agosto de 1984", a partir de la fecha ISO 8601
     * del servidor (`1984-08-01T00:00:00.000Z`). Si la fecha no tiene ese formato, la devuelve tal cual.
     */
    fun releaseDate(isoDate: String): String = try {
        formatter.format(parser.parse(isoDate.take(10))!!)
    } catch (e: ParseException) {
        isoDate
    }
}
