package co.edu.uniandes.vinilos.repositories

import android.content.Context
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistType
import co.edu.uniandes.vinilos.models.Prize
import co.edu.uniandes.vinilos.network.ArtistService
import co.edu.uniandes.vinilos.network.NetworkArtistService
import co.edu.uniandes.vinilos.network.NetworkPrizeService
import co.edu.uniandes.vinilos.network.NetworkServiceAdapter
import co.edu.uniandes.vinilos.network.PrizeService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.text.Collator
import java.util.Locale

/** Fuente de datos de músicos y bandas para los ViewModels; oculta de dónde vienen (hoy, la API REST). */
class ArtistRepository(
    private val service: ArtistService,
    private val prizeService: PrizeService
) {

    /**
     * Músicos y bandas en una sola lista ordenada por nombre. Pide los dos tipos en paralelo; si falla
     * cualquiera de las dos peticiones, lanza la excepción para que el ViewModel muestre el error.
     */
    suspend fun getArtists(): List<Artist> = coroutineScope {
        val musicians = async { service.getMusicians() }
        val bands = async { service.getBands() }
        sortByName(musicians.await() + bands.await())
    }

    /**
     * Un músico o una banda, según [type], con el nombre y la organización de cada premio.
     * Pide el artista y los premios en paralelo. Si falla la red, el servidor o el artista no existe,
     * lanza la excepción. Si solo fallan los premios, devuelve el artista con sus premios sin nombre.
     */
    suspend fun getArtist(id: Int, type: ArtistType): Artist = coroutineScope {
        val prizes = async { getPrizesOrEmpty() }
        val artist = when (type) {
            ArtistType.MUSICIAN -> service.getMusician(id)
            ArtistType.BAND -> service.getBand(id)
        }
        withPrizeNames(artist, prizes.await())
    }

    private suspend fun getPrizesOrEmpty(): List<Prize> = try {
        prizeService.getPrizes()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        fun create(context: Context): ArtistRepository {
            val network = NetworkServiceAdapter.getInstance(context)
            return ArtistRepository(NetworkArtistService(network), NetworkPrizeService(network))
        }

        /** Orden alfabético en español: sin distinguir mayúsculas y con "Á" junto a "A". */
        fun sortByName(artists: List<Artist>): List<Artist> {
            val collator = Collator.getInstance(Locale("es"))
            return artists.sortedWith(compareBy(collator) { it.name })
        }

        /**
         * Completa el nombre y la organización de cada premio del artista cruzando su id con los
         * `performerPrizes` de [prizes]. Un premio que no aparece en [prizes] queda sin nombre.
         */
        fun withPrizeNames(artist: Artist, prizes: List<Prize>): Artist {
            val prizeByAward = prizes.flatMap { prize -> prize.performerPrizeIds.map { it to prize } }.toMap()
            return artist.copy(prizes = artist.prizes.map { award ->
                prizeByAward[award.id]?.let { award.copy(name = it.name, organization = it.organization) } ?: award
            })
        }
    }
}
