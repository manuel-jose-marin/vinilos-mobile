package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import kotlinx.coroutines.CancellationException
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

    init {
        loadAlbum()
    }

    /** Pide el álbum al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadAlbum() {
        _state.value = AlbumDetailState.Loading
        viewModelScope.launch {
            _state.value = try {
                AlbumDetailState.Content(repository.getAlbum(albumId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumDetailState.Error
            }
        }
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
