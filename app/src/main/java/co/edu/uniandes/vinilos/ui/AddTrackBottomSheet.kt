package co.edu.uniandes.vinilos.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.BottomSheetAddTrackBinding
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.validation.TrackField
import co.edu.uniandes.vinilos.validation.TrackForm
import co.edu.uniandes.vinilos.validation.TrackFormError
import co.edu.uniandes.vinilos.validation.TrackFormValidator
import co.edu.uniandes.vinilos.viewmodels.AddTrackState
import co.edu.uniandes.vinilos.viewmodels.AddTrackViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

/** Hoja "Agregar track" (HU08): se abre sobre el detalle del álbum y le devuelve el track creado. */
class AddTrackBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddTrackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTrackViewModel by viewModels {
        AddTrackViewModel.Factory(
            requireArguments().getInt(ARG_ALBUM_ID),
            AlbumRepository.create(requireContext())
        )
    }

    private val validator = TrackFormValidator()

    /** Solo después de tocar "Agregar" se lleva el foco al primer error; al rotar la pantalla no. */
    private var focusFirstError = false

    /** La hoja abre completa y se redimensiona con el teclado, para que los botones sigan a la vista. */
    @Suppress("DEPRECATION")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvAddTrackAlbum.text =
            getString(R.string.add_track_album, requireArguments().getString(ARG_ALBUM_NAME).orEmpty())
        binding.etTrackName.doAfterTextChanged { binding.tilTrackName.error = null }
        binding.etTrackDuration.doAfterTextChanged { binding.tilTrackDuration.error = null }
        binding.btnAddTrackCancel.setOnClickListener { dismiss() }
        binding.btnAddTrackSubmit.setOnClickListener {
            focusFirstError = true
            viewModel.save(readForm())
        }

        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    private fun readForm() = TrackForm(
        name = binding.etTrackName.text?.toString().orEmpty(),
        duration = binding.etTrackDuration.text?.toString().orEmpty()
    )

    private fun render(state: AddTrackState) {
        val isSaving = state == AddTrackState.Saving
        binding.tilTrackName.isEnabled = !isSaving
        binding.tilTrackDuration.isEnabled = !isSaving
        binding.btnAddTrackCancel.isEnabled = !isSaving
        binding.btnAddTrackSubmit.isEnabled = !isSaving
        // Mientras se guarda no se puede cerrar: el track se agregaría sin que el usuario vea el resultado.
        isCancelable = !isSaving

        when (state) {
            is AddTrackState.Editing -> showErrors(state.errors)
            AddTrackState.Saving -> Unit
            is AddTrackState.Added -> finishWith(state.track)
            AddTrackState.SaveError ->
                Snackbar.make(binding.root, R.string.add_track_save_error, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.btnAddTrackSubmit)
                    .show()
        }
    }

    /** Muestra cada error bajo su campo. Al rotar se omiten los que el usuario ya corrigió. */
    private fun showErrors(errors: Map<TrackField, TrackFormError>) {
        val current = validator.validate(readForm())
        errors.filterKeys { it in current }.forEach { (field, error) ->
            val layout = if (field == TrackField.NAME) binding.tilTrackName else binding.tilTrackDuration
            layout.error = getString(error.message)
        }
        if (focusFirstError) {
            focusFirstError = false
            when {
                TrackField.NAME in errors -> binding.etTrackName.requestFocus()
                TrackField.DURATION in errors -> binding.etTrackDuration.requestFocus()
            }
        }
    }

    private fun finishWith(track: Track) {
        setFragmentResult(
            RESULT_KEY,
            bundleOf(
                RESULT_TRACK_ID to track.id,
                RESULT_TRACK_NAME to track.name,
                RESULT_TRACK_DURATION to track.duration
            )
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** Argumentos del destino `addTrackBottomSheet` en el grafo de navegación. */
        const val ARG_ALBUM_ID = "albumId"
        const val ARG_ALBUM_NAME = "albumName"

        /** Resultado que recibe el detalle del álbum con el track creado. */
        const val RESULT_KEY = "add_track_result"
        const val RESULT_TRACK_ID = "track_id"
        const val RESULT_TRACK_NAME = "track_name"
        const val RESULT_TRACK_DURATION = "track_duration"
    }
}
