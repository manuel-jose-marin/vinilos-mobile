package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Estados de la pantalla del detalle de un álbum; la vista muestra solo uno a la vez.
 * Si el álbum no tiene tracks o comentarios, llega como [Content] con esas listas vacías y la vista
 * muestra el mensaje de lista vacía en la sección correspondiente.
 */
sealed interface AlbumDetailState {
    object Loading : AlbumDetailState
    data class Content(val album: Album) : AlbumDetailState
    object Error : AlbumDetailState
}

class AlbumDetailViewModel(
    private val albumId: Int,
    private val repository: AlbumRepository
) : ViewModel() {

    private val _state = MutableLiveData<AlbumDetailState>()
    val state: LiveData<AlbumDetailState> = _state

    /** Carga en curso; se cancela si se pide otra antes de que termine. */
    private var loadJob: Job? = null

    init {
        loadAlbum()
    }

    /** Pide el álbum al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadAlbum() {
        // Un reintento cancela la carga anterior: así no quedan dos peticiones a la vez ni una respuesta
        // vieja que reemplace a la nueva.
        loadJob?.cancel()
        _state.value = AlbumDetailState.Loading
        loadJob = viewModelScope.launch {
            _state.value = try {
                AlbumDetailState.Content(repository.getAlbum(albumId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumDetailState.Error
            }
        }
    }

    /**
     * Agrega al detalle el track que el servidor acaba de crear (HU08), sin volver a pedir el álbum.
     * Si el álbum no está en pantalla (cargando o con error), lo vuelve a cargar para que incluya el track.
     */
    fun onTrackAdded(track: Track) {
        val current = _state.value as? AlbumDetailState.Content ?: return loadAlbum()
        _state.value = AlbumDetailState.Content(current.album.copy(tracks = current.album.tracks + track))
    }

    class Factory(
        private val albumId: Int,
        private val repository: AlbumRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlbumDetailViewModel(albumId, repository) as T
    }
}
