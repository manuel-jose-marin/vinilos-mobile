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
        // El tamaño de la lista no depende de su contenido: así un cambio de datos no vuelve a medir la pantalla.
        binding.rvCollectors.setHasFixedSize(true)
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

    private fun openCollectorDetail(collector: Collector) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.collectorListFragment) return
        navController.navigate(
            R.id.action_collectorListFragment_to_collectorDetailFragment,
            bundleOf(
                CollectorDetailFragment.ARG_COLLECTOR_ID to collector.id,
                CollectorDetailFragment.ARG_COLLECTOR_NAME to collector.name
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCollectors.adapter = null
        _binding = null
    }
}
