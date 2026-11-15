package co.edu.uniandes.vinilos.ui

import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Textos formateados para la pantalla de detalle de un artista (HU04). */
object ArtistDetailText {

    private val utc: TimeZone = TimeZone.getTimeZone("UTC")
    private val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = utc; isLenient = false }
    private val formatter = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "CO")).apply { timeZone = utc }

    /** Texto para el chip de fecha: "Nacido el 16 de julio de 1948" o "Fundada el 1 de enero de 1970". */
    fun formattedDate(artist: Artist): String {
        val dateText = formatDate(artist.date)
        return when (artist.type) {
            ArtistType.MUSICIAN -> "Nacido el $dateText"
            ArtistType.BAND -> "Fundada el $dateText"
        }
    }

    /** Fecha legible en español ("16 de julio de 1948"). Si no parsea, devuelve los primeros 10 caracteres o el texto tal cual. */
    fun formatDate(isoDate: String): String = try {
        formatter.format(parser.parse(isoDate.take(10))!!)
    } catch (e: ParseException) {
        isoDate.take(10)
    }

    /** Año de una fecha ISO 8601 (p. ej. "1975" de "1975-11-21T00:00:00.000Z"). */
    fun year(isoDate: String): String = isoDate.take(4)
}
