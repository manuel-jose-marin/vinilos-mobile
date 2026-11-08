package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistType
import co.edu.uniandes.vinilos.repositories.ArtistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Filtro del listado de artistas (botón segmentado Todos / Músicos / Bandas). */
enum class ArtistFilter {
    ALL, MUSICIANS, BANDS;

    fun matches(artist: Artist): Boolean = when (this) {
        ALL -> true
        MUSICIANS -> artist.type == ArtistType.MUSICIAN
        BANDS -> artist.type == ArtistType.BAND
    }
}

/** Estados de la pantalla del listado de artistas; la vista muestra solo uno a la vez. */
sealed interface ArtistListState {
    object Loading : ArtistListState

    /**
     * Artistas que pasan el [filter] activo, en orden alfabético. Puede venir vacía si el servidor tiene
     * artistas pero ninguno del tipo filtrado; [Empty] es solo cuando el servidor no tiene ninguno.
     */
    data class Content(val artists: List<Artist>, val filter: ArtistFilter) : ArtistListState
    object Empty : ArtistListState
    object Error : ArtistListState
}

class ArtistListViewModel(private val repository: ArtistRepository) : ViewModel() {

    private val _state = MutableLiveData<ArtistListState>()
    val state: LiveData<ArtistListState> = _state

    /** Lista completa ya descargada; los filtros se aplican sobre ella sin volver a llamar a la API. */
    private var allArtists: List<Artist> = emptyList()

    /** Filtro seleccionado; se conserva al reintentar o mientras carga. */
    var filter: ArtistFilter = ArtistFilter.ALL
        private set

    init {
        loadArtists()
    }

    /** Pide músicos y bandas al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadArtists() {
        _state.value = ArtistListState.Loading
        viewModelScope.launch {
            try {
                allArtists = repository.getArtists()
                showArtists()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = ArtistListState.Error
            }
        }
    }

    /** Cambia el filtro en memoria. Si la lista aún no ha llegado, se aplica cuando llegue. */
    fun setFilter(filter: ArtistFilter) {
        if (filter == this.filter) return
        this.filter = filter
        if (_state.value is ArtistListState.Content) showArtists()
    }

    private fun showArtists() {
        _state.value = if (allArtists.isEmpty()) {
            ArtistListState.Empty
        } else {
            ArtistListState.Content(allArtists.filter(filter::matches), filter)
        }
    }

    class Factory(private val repository: ArtistRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ArtistListViewModel(repository) as T
    }
}
