package co.edu.uniandes.vinilos.ui

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Textos de la pantalla del detalle de un álbum (HU02). */
object AlbumDetailText {

    private val spanish = Locale("es", "CO")

    /**
     * Fecha de lanzamiento legible, por ejemplo "1 de agosto de 1984", a partir de la fecha ISO 8601
     * del servidor (`1984-08-01T00:00:00.000Z`). Si la fecha no tiene ese formato, la devuelve tal cual.
     */
    fun releaseDate(isoDate: String): String {
        val utc = TimeZone.getTimeZone("UTC")
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utc; isLenient = false }
        val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", spanish).apply { timeZone = utc }
        return try {
            formatter.format(parser.parse(isoDate.take(10))!!)
        } catch (e: ParseException) {
            isoDate
        }
    }
}
