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
import co.edu.uniandes.vinilos.databinding.FragmentAlbumListBinding
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.viewmodels.AlbumListState
import co.edu.uniandes.vinilos.viewmodels.AlbumListViewModel

/** Pantalla "Álbumes" (HU01): catálogo en cuadrícula con estados de carga, vacío y error. */
class AlbumListFragment : Fragment() {

    private var _binding: FragmentAlbumListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumListViewModel by viewModels {
        AlbumListViewModel.Factory(AlbumRepository.create(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = AlbumAdapter(::openAlbumDetail)
        binding.rvAlbums.adapter = adapter
        binding.btnRetry.setOnClickListener { viewModel.loadAlbums() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.layoutLoading.isVisible = state is AlbumListState.Loading
            binding.rvAlbums.isVisible = state is AlbumListState.Content
            binding.tvEmpty.isVisible = state is AlbumListState.Empty
            binding.layoutError.isVisible = state is AlbumListState.Error
            if (state is AlbumListState.Content) adapter.submitList(state.albums)
        }
    }

    private fun openAlbumDetail(album: Album) {
        val navController = findNavController()
        // Evita navegar dos veces si se tocan dos tarjetas muy rápido.
        if (navController.currentDestination?.id != R.id.albumListFragment) return
        navController.navigate(
            R.id.action_albumListFragment_to_albumDetailFragment,
            bundleOf(
                AlbumDetailFragment.ARG_ALBUM_ID to album.id,
                AlbumDetailFragment.ARG_ALBUM_NAME to album.name
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvAlbums.adapter = null
        _binding = null
    }
}
