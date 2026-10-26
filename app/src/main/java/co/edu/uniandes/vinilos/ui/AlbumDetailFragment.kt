package co.edu.uniandes.vinilos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.databinding.FragmentAlbumDetailBinding

/**
 * Pantalla "Detalle del álbum" (HU02). Por ahora es el destino al que navega el listado: muestra el
 * nombre del álbum y permite volver. La vista completa se implementa en #21.
 */
class AlbumDetailFragment : Fragment() {

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireArguments().getString(ARG_ALBUM_NAME)?.let { binding.toolbarAlbumDetail.title = it }
        binding.toolbarAlbumDetail.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** Id del álbum a mostrar (argumento `albumId` del grafo de navegación). */
        const val ARG_ALBUM_ID = "albumId"

        /** Nombre del álbum, para mostrarlo en la barra superior mientras carga el detalle. */
        const val ARG_ALBUM_NAME = "albumName"
    }
}
