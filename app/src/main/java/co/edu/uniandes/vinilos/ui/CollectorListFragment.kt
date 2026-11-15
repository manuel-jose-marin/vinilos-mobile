package co.edu.uniandes.vinilos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentCollectorListBinding
import co.edu.uniandes.vinilos.models.Collector
import co.edu.uniandes.vinilos.repositories.CollectorRepository
import co.edu.uniandes.vinilos.viewmodels.CollectorListState
import co.edu.uniandes.vinilos.viewmodels.CollectorListViewModel

/** Pantalla "Coleccionistas" (HU05): total y filas de coleccionistas, con estados de carga, vacío y error. */
class CollectorListFragment : Fragment() {

    private var _binding: FragmentCollectorListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectorListViewModel by viewModels {
        CollectorListViewModel.Factory(CollectorRepository.create(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CollectorAdapter(::openCollectorDetail)
        binding.rvCollectors.adapter = adapter
        binding.btnRetry.setOnClickListener { viewModel.loadCollectors() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.layoutLoading.isVisible = state is CollectorListState.Loading
            binding.layoutContent.isVisible = state is CollectorListState.Content
            binding.tvEmpty.isVisible = state is CollectorListState.Empty
            binding.layoutError.isVisible = state is CollectorListState.Error

            if (state is CollectorListState.Content) {
                val total = state.collectors.size
                binding.tvCollectorsCount.text =
                    resources.getQuantityString(R.plurals.collectors_count, total, total)
                adapter.submitList(state.collectors)
            }
        }
    }

    /**
     * Punto de entrada al detalle del coleccionista (HU05, criterio 5). La pantalla de detalle y su
     * destino en el grafo de navegación llegan con HU06 (#52); hasta entonces el toque no navega.
     */
    @Suppress("UNUSED_PARAMETER")
    private fun openCollectorDetail(collector: Collector) = Unit

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCollectors.adapter = null
        _binding = null
    }
}
