package co.edu.uniandes.vinilos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.ItemArtistBinding
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistType
import com.bumptech.glide.Glide

/** Tarjetas del listado de artistas (HU03). Al tocar una tarjeta llama a [onArtistClick] con su artista. */
class ArtistAdapter(
    private val onArtistClick: (Artist) -> Unit
) : ListAdapter<Artist, ArtistAdapter.ArtistViewHolder>(ArtistDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            val context = binding.root.context
            binding.tvArtistName.text = artist.name
            binding.chipArtistType.setText(
                when (artist.type) {
                    ArtistType.MUSICIAN -> R.string.artist_type_musician
                    ArtistType.BAND -> R.string.artist_type_band
                }
            )
            val albumCount = artist.albums.size
            binding.tvArtistAlbumsCount.text =
                context.resources.getQuantityString(R.plurals.artist_albums_count, albumCount, albumCount)

            // Mientras carga la foto, si falla o si no tiene URL, se ve la imagen genérica.
            Glide.with(binding.ivArtistImage)
                .load(artist.image.ifBlank { null })
                .placeholder(R.drawable.ic_nav_artists)
                .error(R.drawable.ic_nav_artists)
                .fallback(R.drawable.ic_nav_artists)
                .into(binding.ivArtistImage)

            binding.root.setOnClickListener { onArtistClick(artist) }
        }
    }

    private object ArtistDiff : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist) =
            oldItem.id == newItem.id && oldItem.type == newItem.type

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist) = oldItem == newItem
    }
}
