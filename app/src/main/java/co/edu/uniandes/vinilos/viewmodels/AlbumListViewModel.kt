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

/** Estados de la pantalla del listado de álbumes; la vista muestra solo uno a la vez. */
sealed interface AlbumListState {
    object Loading : AlbumListState
    data class Content(val albums: List<Album>) : AlbumListState
    object Empty : AlbumListState
    object Error : AlbumListState
}

class AlbumListViewModel(private val repository: AlbumRepository) : ViewModel() {

    private val _state = MutableLiveData<AlbumListState>()
    val state: LiveData<AlbumListState> = _state

    init {
        loadAlbums()
    }

    /** Pide el catálogo al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadAlbums() {
        _state.value = AlbumListState.Loading
        viewModelScope.launch {
            _state.value = try {
                val albums = repository.getAlbums()
                if (albums.isEmpty()) AlbumListState.Empty else AlbumListState.Content(albums)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AlbumListState.Error
            }
        }
    }

    class Factory(private val repository: AlbumRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AlbumListViewModel(repository) as T
    }
}
