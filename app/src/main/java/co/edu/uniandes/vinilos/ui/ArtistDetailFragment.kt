package co.edu.uniandes.vinilos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.databinding.FragmentArtistDetailBinding

/**
 * Pantalla "Detalle del artista" (HU04). Por ahora solo recibe los argumentos del listado (HU03) y muestra
 * el nombre en la barra superior; el ViewModel y el contenido se conectan en #36.
 */
class ArtistDetailFragment : Fragment() {

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireArguments().getString(ARG_ARTIST_NAME)?.let { binding.toolbarArtistDetail.title = it }
        binding.toolbarArtistDetail.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** Id del artista a mostrar (argumento `artistId` del grafo de navegación). */
        const val ARG_ARTIST_ID = "artistId"

        /** Tipo del artista, `MUSICIAN` o `BAND` ([co.edu.uniandes.vinilos.models.ArtistType.name]). */
        const val ARG_ARTIST_TYPE = "artistType"

        /** Nombre del artista, para mostrarlo en la barra superior mientras carga el detalle. */
        const val ARG_ARTIST_NAME = "artistName"
    }
}
