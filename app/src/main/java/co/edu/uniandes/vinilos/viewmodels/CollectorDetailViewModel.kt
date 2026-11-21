package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.CollectorDetail
import co.edu.uniandes.vinilos.repositories.CollectorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Estados de la pantalla del detalle de un coleccionista; la vista muestra solo uno a la vez.
 * Si no tiene favoritos, álbumes o comentarios, llega como [Content] con esas listas vacías y la vista muestra el
 * mensaje de lista vacía en la sección correspondiente.
 */
sealed interface CollectorDetailState {
    object Loading : CollectorDetailState
    data class Content(val detail: CollectorDetail) : CollectorDetailState
    object Error : CollectorDetailState
}

class CollectorDetailViewModel(
    private val collectorId: Int,
    private val repository: CollectorRepository
) : ViewModel() {

    private val _state = MutableLiveData<CollectorDetailState>()
    val state: LiveData<CollectorDetailState> = _state

    /** Carga en curso; se cancela si se pide otra antes de que termine. */
    private var loadJob: Job? = null

    init {
        loadCollector()
    }

    /** Pide el detalle al repositorio; también se usa para "Reintentar" tras un error. */
    fun loadCollector() {
        // Un reintento cancela la carga anterior: así no quedan dos peticiones a la vez ni una respuesta
        // vieja que reemplace a la nueva.
        loadJob?.cancel()
        _state.value = CollectorDetailState.Loading
        loadJob = viewModelScope.launch {
            _state.value = try {
                CollectorDetailState.Content(repository.getCollectorDetail(collectorId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CollectorDetailState.Error
            }
        }
    }

    class Factory(
        private val collectorId: Int,
        private val repository: CollectorRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CollectorDetailViewModel(collectorId, repository) as T
    }
}
