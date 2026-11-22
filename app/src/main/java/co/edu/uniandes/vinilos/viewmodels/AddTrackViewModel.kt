package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.NewTrack
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.validation.TrackField
import co.edu.uniandes.vinilos.validation.TrackForm
import co.edu.uniandes.vinilos.validation.TrackFormError
import co.edu.uniandes.vinilos.validation.TrackFormValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Estados de la hoja "Agregar track" (HU08); la vista muestra solo uno a la vez. */
sealed interface AddTrackState {
    /** El usuario está llenando la hoja; [errors] tiene el error de cada campo inválido (vacío al empezar). */
    data class Editing(val errors: Map<TrackField, TrackFormError> = emptyMap()) : AddTrackState

    /** Se envió `POST /albums/{id}/tracks`: "Agregar" queda deshabilitado. */
    object Saving : AddTrackState

    /** El servidor agregó el track; la hoja se cierra y lo devuelve al detalle, que muestra "Track «X» agregado". */
    data class Added(val track: Track) : AddTrackState

    /** Falló la red o el servidor. La hoja sigue abierta con los datos y muestra el error; se puede reintentar. */
    object SaveError : AddTrackState
}

class AddTrackViewModel(
    private val albumId: Int,
    private val repository: AlbumRepository,
    private val validator: TrackFormValidator = TrackFormValidator()
) : ViewModel() {

    private val _state = MutableLiveData<AddTrackState>(AddTrackState.Editing())
    val state: LiveData<AddTrackState> = _state

    /**
     * Valida la hoja y, si es válida, agrega el track al álbum. Una hoja con errores no hace ninguna petición.
     * Mientras se guarda, o después de agregarlo, no hace nada: así un doble toque no agrega el track dos veces.
     * Tras un [AddTrackState.SaveError] se puede volver a llamar con los mismos datos para reintentar.
     */
    fun save(form: TrackForm) {
        val current = _state.value
        if (current == AddTrackState.Saving || current is AddTrackState.Added) return

        val errors = validator.validate(form)
        if (errors.isNotEmpty()) {
            _state.value = AddTrackState.Editing(errors)
            return
        }

        _state.value = AddTrackState.Saving
        viewModelScope.launch {
            _state.value = try {
                AddTrackState.Added(repository.addTrack(albumId, toNewTrack(form)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AddTrackState.SaveError
            }
        }
    }

    class Factory(
        private val albumId: Int,
        private val repository: AlbumRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddTrackViewModel(albumId, repository) as T
    }

    companion object {
        /** Lo que se envía a `POST /albums/{id}/tracks` a partir de una hoja ya validada, sin espacios sobrantes. */
        fun toNewTrack(form: TrackForm) = NewTrack(name = form.name.trim(), duration = form.duration.trim())
    }
}
