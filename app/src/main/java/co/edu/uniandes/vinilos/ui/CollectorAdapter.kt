package co.edu.uniandes.vinilos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.ItemCollectorBinding
import co.edu.uniandes.vinilos.models.Collector

/** Filas del listado de coleccionistas (HU05). Al tocar una fila llama a [onCollectorClick] con su coleccionista. */
class CollectorAdapter(
    private val onCollectorClick: (Collector) -> Unit
) : ListAdapter<Collector, CollectorAdapter.CollectorViewHolder>(CollectorDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectorViewHolder {
        val binding = ItemCollectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CollectorViewHolder(private val binding: ItemCollectorBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(collector: Collector) {
            // BackVynils no guarda foto de los coleccionistas, así que el avatar siempre son sus iniciales.
            binding.tvCollectorInitials.text = CollectorText.initials(collector.name)
            binding.tvCollectorName.text = collector.name
            binding.tvCollectorEmail.text = collector.email
            binding.tvCollectorAlbumsCount.text = binding.root.resources.getQuantityString(
                R.plurals.collector_albums_count, collector.albumCount, collector.albumCount
            )
            binding.root.setOnClickListener { onCollectorClick(collector) }
        }
    }

    private object CollectorDiff : DiffUtil.ItemCallback<Collector>() {
        override fun areItemsTheSame(oldItem: Collector, newItem: Collector) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Collector, newItem: Collector) = oldItem == newItem
    }
}
