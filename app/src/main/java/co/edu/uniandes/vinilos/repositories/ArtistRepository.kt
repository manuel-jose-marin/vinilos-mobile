package co.edu.uniandes.vinilos.repositories

import android.content.Context
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.network.ArtistService
import co.edu.uniandes.vinilos.network.NetworkArtistService
import co.edu.uniandes.vinilos.network.NetworkServiceAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.Collator
import java.util.Locale

/** Fuente de datos de músicos y bandas para los ViewModels; oculta de dónde vienen (hoy, la API REST). */
class ArtistRepository(private val service: ArtistService) {

    /**
     * Músicos y bandas en una sola lista ordenada por nombre. Pide los dos tipos en paralelo; si falla
     * cualquiera de las dos peticiones, lanza la excepción para que el ViewModel muestre el error.
     */
    suspend fun getArtists(): List<Artist> = coroutineScope {
        val musicians = async { service.getMusicians() }
        val bands = async { service.getBands() }
        sortByName(musicians.await() + bands.await())
    }

    companion object {
        fun create(context: Context): ArtistRepository =
            ArtistRepository(NetworkArtistService(NetworkServiceAdapter.getInstance(context)))

        /** Orden alfabético en español: sin distinguir mayúsculas y con "Á" junto a "A". */
        fun sortByName(artists: List<Artist>): List<Artist> {
            val collator = Collator.getInstance(Locale("es"))
            return artists.sortedWith(compareBy(collator) { it.name })
        }
    }
}
