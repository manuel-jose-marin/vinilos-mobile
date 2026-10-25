package co.edu.uniandes.vinilos.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.ItemAlbumBinding
import co.edu.uniandes.vinilos.models.Album
import com.bumptech.glide.Glide

/** Tarjetas del catálogo de álbumes (HU01). Al tocar una tarjeta llama a [onAlbumClick] con su álbum. */
class AlbumAdapter(
    private val onAlbumClick: (Album) -> Unit
) : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(AlbumDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            val context = binding.root.context
            binding.tvAlbumName.text = album.name
            binding.tvAlbumArtist.text =
                AlbumCardText.artists(album).ifEmpty { context.getString(R.string.album_unknown_artist) }
            binding.tvAlbumYearGenre.text = AlbumCardText.yearAndGenre(album)

            // Mientras carga la portada, si falla o si no tiene URL, se ve la imagen genérica.
            Glide.with(binding.ivAlbumCover)
                .load(album.cover.ifBlank { null })
                .placeholder(R.drawable.ic_album_placeholder)
                .error(R.drawable.ic_album_placeholder)
                .fallback(R.drawable.ic_album_placeholder)
                .into(binding.ivAlbumCover)

            binding.root.setOnClickListener { onAlbumClick(album) }
        }
    }

    private object AlbumDiff : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Album, newItem: Album) = oldItem == newItem
    }
}
