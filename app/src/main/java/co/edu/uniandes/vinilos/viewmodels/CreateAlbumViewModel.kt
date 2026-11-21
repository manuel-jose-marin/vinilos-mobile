package co.edu.uniandes.vinilos.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.NewAlbum
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.validation.AlbumField
import co.edu.uniandes.vinilos.validation.AlbumForm
import co.edu.uniandes.vinilos.validation.AlbumFormError
import co.edu.uniandes.vinilos.validation.AlbumFormValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Estados del formulario de creación de álbum (HU07); la vista muestra solo uno a la vez. */
sealed interface CreateAlbumState {
    /** El usuario está llenando el formulario; [errors] tiene el error de cada campo inválido (vacío al empezar). */
    data class Editing(val errors: Map<AlbumField, AlbumFormError> = emptyMap()) : CreateAlbumState

    /** Se envió `POST /albums`: el botón dice "Guardando…" y el formulario queda deshabilitado. */
    object Saving : CreateAlbumState

    /** El servidor creó el álbum; la vista vuelve al catálogo con el mensaje "Álbum «X» creado". */
    data class Saved(val album: Album) : CreateAlbumState

    /** Falló la red o el servidor. La vista muestra el error y conserva lo escrito para que el usuario reintente. */
    object SaveError : CreateAlbumState
}

class CreateAlbumViewModel(
    private val repository: AlbumRepository,
    private val validator: AlbumFormValidator = AlbumFormValidator()
) : ViewModel() {

    private val _state = MutableLiveData<CreateAlbumState>(CreateAlbumState.Editing())
    val state: LiveData<CreateAlbumState> = _state

    /**
     * Valida el formulario y, si es válido, crea el álbum. Un formulario con errores no hace ninguna petición.
     * Mientras se guarda, o después de guardar, no hace nada: así un doble toque no crea dos álbumes.
     * Tras un [CreateAlbumState.SaveError] se puede volver a llamar con el mismo formulario para reintentar.
     */
    fun save(form: AlbumForm) {
        val current = _state.value
        if (current == CreateAlbumState.Saving || current is CreateAlbumState.Saved) return

        val errors = validator.validate(form)
        if (errors.isNotEmpty()) {
            _state.value = CreateAlbumState.Editing(errors)
            return
        }

        _state.value = CreateAlbumState.Saving
        viewModelScope.launch {
            _state.value = try {
                CreateAlbumState.Saved(repository.createAlbum(toNewAlbum(form)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CreateAlbumState.SaveError
            }
        }
    }

    class Factory(private val repository: AlbumRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CreateAlbumViewModel(repository) as T
    }

    companion object {
        /**
         * Lo que se envía a `POST /albums` a partir de un formulario ya validado: sin espacios al inicio ni al final,
         * y la fecha como `yyyy-MM-dd`.
         */
        fun toNewAlbum(form: AlbumForm) = NewAlbum(
            name = form.name.trim(),
            cover = form.cover.trim(),
            releaseDate = releaseDateText(requireNotNull(form.releaseDate) { "Formulario sin fecha" }),
            description = form.description.trim(),
            genre = requireNotNull(form.genre) { "Formulario sin género" },
            recordLabel = requireNotNull(form.recordLabel) { "Formulario sin sello" }
        )

        /**
         * Día elegido en el selector como `yyyy-MM-dd`. El selector entrega la medianoche UTC de ese día, así que se
         * formatea en UTC: con la zona del teléfono (UTC-5 en Colombia) quedaría el día anterior.
         */
        fun releaseDateText(utcMidnightMillis: Long): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .format(Date(utcMidnightMillis))
    }
}
