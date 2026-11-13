package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.repositories.CollectorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Estados de la pantalla del listado de coleccionistas; la vista muestra solo uno a la vez. */
sealed interface CollectorListState {
    object Loading : CollectorListState
    data class Content(val collectors: List<Collector>) : CollectorListState
    object Empty : CollectorListState
    object Error : CollectorListState
}

class CollectorListViewModel(private val repository: CollectorRepository) : ViewModel() {

    private val _state = MutableLiveData<CollectorListState>()
    val state: LiveData<CollectorListState> = _state

    init {
        loadCollectors()
    }

    /** Pide el listado al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadCollectors() {
        _state.value = CollectorListState.Loading
        viewModelScope.launch {
            _state.value = try {
                val collectors = repository.getCollectors()
                if (collectors.isEmpty()) CollectorListState.Empty else CollectorListState.Content(collectors)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CollectorListState.Error
            }
        }
    }

    class Factory(private val repository: CollectorRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CollectorListViewModel(repository) as T
    }
}
