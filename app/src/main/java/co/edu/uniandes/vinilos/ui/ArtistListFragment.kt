package co.edu.uniandes.vinilos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentArtistListBinding
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.repositories.ArtistRepository
import co.edu.uniandes.vinilos.viewmodels.ArtistFilter
import co.edu.uniandes.vinilos.viewmodels.ArtistListState
import co.edu.uniandes.vinilos.viewmodels.ArtistListViewModel

/** Pantalla "Artistas" (HU03): músicos y bandas con filtro Todos / Músicos / Bandas y estados de carga, vacío y error. */
class ArtistListFragment : Fragment() {

    private var _binding: FragmentArtistListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistListViewModel by viewModels {
        ArtistListViewModel.Factory(ArtistRepository.create(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ArtistAdapter(::openArtistDetail)
        binding.rvArtists.adapter = adapter
        binding.btnRetry.setOnClickListener { viewModel.loadArtists() }

        // Al volver del detalle se restaura el filtro que ya tenía el ViewModel.
        binding.toggleArtistFilter.check(buttonFor(viewModel.filter))
        binding.toggleArtistFilter.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) viewModel.setFilter(filterFor(checkedId))
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val filteredEmpty = state is ArtistListState.Content && state.artists.isEmpty()
            binding.layoutLoading.isVisible = state is ArtistListState.Loading
            binding.rvArtists.isVisible = state is ArtistListState.Content && !filteredEmpty
            binding.tvEmpty.isVisible = state is ArtistListState.Empty || filteredEmpty
            binding.layoutError.isVisible = state is ArtistListState.Error
            // El filtro solo tiene sentido cuando hay artistas para filtrar.
            binding.toggleArtistFilter.isEnabled = state is ArtistListState.Content

            when (state) {
                is ArtistListState.Content -> {
                    binding.tvEmpty.setText(R.string.artists_filter_empty)
                    adapter.submitList(state.artists)
                }
                ArtistListState.Empty -> binding.tvEmpty.setText(R.string.artists_empty)
                else -> Unit
            }
        }
    }

    private fun openArtistDetail(artist: Artist) {
        val navController = findNavController()
        // Evita navegar dos veces si se tocan dos tarjetas muy rápido.
        if (navController.currentDestination?.id != R.id.artistListFragment) return
        navController.navigate(
            R.id.action_artistListFragment_to_artistDetailFragment,
            bundleOf(
                ArtistDetailFragment.ARG_ARTIST_ID to artist.id,
                ArtistDetailFragment.ARG_ARTIST_TYPE to artist.type.name,
                ArtistDetailFragment.ARG_ARTIST_NAME to artist.name
            )
        )
    }

    private fun buttonFor(filter: ArtistFilter): Int = when (filter) {
        ArtistFilter.ALL -> R.id.btn_filter_all
        ArtistFilter.MUSICIANS -> R.id.btn_filter_musicians
        ArtistFilter.BANDS -> R.id.btn_filter_bands
    }

    private fun filterFor(buttonId: Int): ArtistFilter = when (buttonId) {
        R.id.btn_filter_musicians -> ArtistFilter.MUSICIANS
        R.id.btn_filter_bands -> ArtistFilter.BANDS
        else -> ArtistFilter.ALL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvArtists.adapter = null
        _binding = null
    }
}
