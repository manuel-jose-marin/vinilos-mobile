package co.edu.uniandes.vinilos.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentCreateAlbumBinding
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.validation.AlbumField
import co.edu.uniandes.vinilos.validation.AlbumForm
import co.edu.uniandes.vinilos.validation.AlbumFormError
import co.edu.uniandes.vinilos.validation.AlbumFormValidator
import co.edu.uniandes.vinilos.viewmodels.CreateAlbumState
import co.edu.uniandes.vinilos.viewmodels.CreateAlbumViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Pantalla "Crear álbum" (HU07): formulario con vista previa de la portada, validación por campo y guardado. */
class CreateAlbumFragment : Fragment() {

    private var _binding: FragmentCreateAlbumBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateAlbumViewModel by viewModels {
        CreateAlbumViewModel.Factory(AlbumRepository.create(requireContext()))
    }

    private val validator = AlbumFormValidator()

    /** Día elegido en el selector, en milisegundos a la medianoche UTC (lo que espera [AlbumForm.releaseDate]). */
    private var releaseDate: Long? = null

    /** Solo después de tocar "Crear álbum" se lleva el foco al primer error; al rotar la pantalla no. */
    private var focusFirstError = false

    private var previewJob: Job? = null

    /** Mientras se guarda no se puede salir: el álbum se crearía sin que el usuario vea el resultado. */
    private val blockBackWhileSaving = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        releaseDate = savedInstanceState
            ?.takeIf { it.containsKey(STATE_RELEASE_DATE) }
            ?.getLong(STATE_RELEASE_DATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, blockBackWhileSaving)
        binding.toolbarCreateAlbum.setNavigationOnClickListener { goBack() }
        binding.btnCreateAlbumCancel.setOnClickListener { goBack() }
        binding.btnCreateAlbumSubmit.setOnClickListener {
            focusFirstError = true
            viewModel.save(readForm())
        }

        setUpErrorClearing()
        setUpCoverPreview()
        setUpDatePicker()
        showReleaseDate()

        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    /** Se cargan aquí porque restaurar el texto filtraría el desplegable y dejaría solo la opción elegida. */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.actvAlbumGenre.setSimpleItems(AlbumFormValidator.GENRES.toTypedArray())
        binding.actvAlbumRecordLabel.setSimpleItems(AlbumFormValidator.RECORD_LABELS.toTypedArray())
    }

    private fun setUpErrorClearing() {
        binding.etAlbumName.doAfterTextChanged { binding.tilAlbumName.error = null }
        binding.etAlbumDescription.doAfterTextChanged { binding.tilAlbumDescription.error = null }
        binding.actvAlbumGenre.doAfterTextChanged { binding.tilAlbumGenre.error = null }
        binding.actvAlbumRecordLabel.doAfterTextChanged { binding.tilAlbumRecordLabel.error = null }
    }

    /** La vista previa espera a que el usuario deje de escribir para no descargar una imagen por cada letra. */
    private fun setUpCoverPreview() {
        binding.etAlbumCover.doAfterTextChanged { text ->
            binding.tilAlbumCover.error = null
            previewJob?.cancel()
            previewJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(PREVIEW_DELAY_MS)
                showCoverPreview(text?.toString().orEmpty().trim())
            }
        }
    }

    private fun showCoverPreview(url: String) {
        binding.tvCoverPreviewError.isVisible = false
        Glide.with(binding.ivCoverPreview).clear(binding.ivCoverPreview)
        val isValid = validator.coverError(url) == null
        binding.layoutCoverPreview.isVisible = isValid
        if (!isValid) return

        Glide.with(binding.ivCoverPreview)
            .load(url)
            .placeholder(R.drawable.ic_album_placeholder)
            .error(R.drawable.ic_album_placeholder)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    _binding?.tvCoverPreviewError?.isVisible = true
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    _binding?.tvCoverPreviewError?.isVisible = false
                    return false
                }
            })
            .into(binding.ivCoverPreview)
    }

    private fun setUpDatePicker() {
        binding.etAlbumReleaseDate.setOnClickListener { openDatePicker() }
        binding.tilAlbumReleaseDate.setEndIconOnClickListener { openDatePicker() }
        // Si la pantalla rotó con el selector abierto, el sistema lo recrea y hay que volver a escuchar su resultado.
        (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<*>)
            ?.let(::listenToDatePicker)
    }

    private fun openDatePicker() {
        if (childFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        // No se limita a fechas pasadas: elegir una futura debe mostrar "La fecha no puede ser futura" (criterio 7).
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.album_form_release_date_hint)
            .setSelection(releaseDate ?: AlbumFormValidator.todayAsUtcMidnight())
            .build()
        listenToDatePicker(picker)
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun listenToDatePicker(picker: MaterialDatePicker<*>) {
        picker.addOnPositiveButtonClickListener { selection ->
            releaseDate = selection as Long
            binding.tilAlbumReleaseDate.error = null
            showReleaseDate()
        }
    }

    private fun showReleaseDate() {
        binding.etAlbumReleaseDate.setText(releaseDate?.let(CreateAlbumViewModel::releaseDateText).orEmpty())
    }

    private fun readForm() = AlbumForm(
        name = binding.etAlbumName.text?.toString().orEmpty(),
        cover = binding.etAlbumCover.text?.toString().orEmpty(),
        releaseDate = releaseDate,
        description = binding.etAlbumDescription.text?.toString().orEmpty(),
        genre = binding.actvAlbumGenre.text?.toString()?.ifEmpty { null },
        recordLabel = binding.actvAlbumRecordLabel.text?.toString()?.ifEmpty { null }
    )

    private fun render(state: CreateAlbumState) {
        val isSaving = state == CreateAlbumState.Saving
        setFormEnabled(!isSaving)
        blockBackWhileSaving.isEnabled = isSaving
        binding.btnCreateAlbumSubmit.setText(if (isSaving) R.string.album_form_saving else R.string.album_form_submit)

        when (state) {
            is CreateAlbumState.Editing -> showErrors(state.errors)
            CreateAlbumState.Saving -> Unit
            is CreateAlbumState.Saved -> finishWith(state.album.name)
            CreateAlbumState.SaveError ->
                Snackbar.make(binding.root, R.string.album_form_save_error, Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.btnCreateAlbumSubmit)
                    .show()
        }
    }

    private fun setFormEnabled(enabled: Boolean) {
        fieldLayouts().values.forEach { it.isEnabled = enabled }
        binding.btnCreateAlbumCancel.isEnabled = enabled
        binding.btnCreateAlbumSubmit.isEnabled = enabled
    }

    /** Muestra cada error bajo su campo. Al rotar se omiten los que el usuario ya corrigió. */
    private fun showErrors(errors: Map<AlbumField, AlbumFormError>) {
        val current = validator.validate(readForm())
        val layouts = fieldLayouts()
        errors.filterKeys { it in current }.forEach { (field, error) ->
            layouts.getValue(field).error = getString(error.message)
        }
        if (focusFirstError) {
            focusFirstError = false
            AlbumField.entries.firstOrNull { it in errors }?.let { focusField(layouts.getValue(it)) }
        }
    }

    /** La fecha y los desplegables no toman foco al tocar, así que además se desplaza hasta el campo. */
    private fun focusField(field: TextInputLayout) {
        field.editText?.requestFocus()
        binding.scrollCreateAlbum.post { _binding?.scrollCreateAlbum?.smoothScrollTo(0, field.top) }
    }

    private fun fieldLayouts(): Map<AlbumField, TextInputLayout> = mapOf(
        AlbumField.NAME to binding.tilAlbumName,
        AlbumField.COVER to binding.tilAlbumCover,
        AlbumField.RELEASE_DATE to binding.tilAlbumReleaseDate,
        AlbumField.DESCRIPTION to binding.tilAlbumDescription,
        AlbumField.GENRE to binding.tilAlbumGenre,
        AlbumField.RECORD_LABEL to binding.tilAlbumRecordLabel
    )

    private fun finishWith(albumName: String) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.createAlbumFragment) return
        setFragmentResult(RESULT_KEY, bundleOf(RESULT_ALBUM_NAME to albumName))
        navController.navigateUp()
    }

    private fun goBack() {
        if (viewModel.state.value != CreateAlbumState.Saving) findNavController().navigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        releaseDate?.let { outState.putLong(STATE_RELEASE_DATE, it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previewJob?.cancel()
        Glide.with(binding.ivCoverPreview).clear(binding.ivCoverPreview)
        _binding = null
    }

    companion object {
        /** Resultado que recibe el listado al crear un álbum, para mostrar la confirmación y recargar. */
        const val RESULT_KEY = "create_album_result"
        const val RESULT_ALBUM_NAME = "album_name"

        private const val DATE_PICKER_TAG = "release_date_picker"
        private const val STATE_RELEASE_DATE = "release_date"
        private const val PREVIEW_DELAY_MS = 400L
    }
}
