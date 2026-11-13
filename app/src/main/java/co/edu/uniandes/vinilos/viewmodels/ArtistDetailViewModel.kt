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

/**
 * Estados de la pantalla del detalle de un artista; la vista muestra solo uno a la vez.
 * Si el artista no tiene álbumes, premios o integrantes, llega como [Content] con esas listas vacías y la
 * vista muestra el mensaje de lista vacía en la sección correspondiente.
 */
sealed interface ArtistDetailState {
    object Loading : ArtistDetailState
    data class Content(val artist: Artist) : ArtistDetailState
    object Error : ArtistDetailState
}

class ArtistDetailViewModel(
    private val artistId: Int,
    private val artistType: ArtistType,
    private val repository: ArtistRepository
) : ViewModel() {

    private val _state = MutableLiveData<ArtistDetailState>()
    val state: LiveData<ArtistDetailState> = _state

    init {
        loadArtist()
    }

    /** Pide el artista al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadArtist() {
        _state.value = ArtistDetailState.Loading
        viewModelScope.launch {
            _state.value = try {
                ArtistDetailState.Content(repository.getArtist(artistId, artistType))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ArtistDetailState.Error
            }
        }
    }

    class Factory(
        private val artistId: Int,
        private val artistType: ArtistType,
        private val repository: ArtistRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ArtistDetailViewModel(artistId, artistType, repository) as T
    }
}
