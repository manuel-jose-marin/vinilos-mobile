package co.edu.uniandes.vinilos.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentCollectorDetailBinding
import co.edu.uniandes.vinilos.databinding.ItemCollectorAlbumBinding
import co.edu.uniandes.vinilos.databinding.ItemCollectorPerformerBinding
import co.edu.uniandes.vinilos.databinding.ItemCommentBinding
import co.edu.uniandes.vinilos.models.CollectorAlbum
import co.edu.uniandes.vinilos.models.CollectorDetail
import co.edu.uniandes.vinilos.models.Comment
import co.edu.uniandes.vinilos.models.Performer
import co.edu.uniandes.vinilos.repositories.CollectorRepository
import co.edu.uniandes.vinilos.viewmodels.CollectorDetailState
import co.edu.uniandes.vinilos.viewmodels.CollectorDetailViewModel
import com.bumptech.glide.Glide

/** Pantalla "Detalle del coleccionista" (HU06): datos de contacto, artistas favoritos, colección y comentarios. */
class CollectorDetailFragment : Fragment() {

    private var _binding: FragmentCollectorDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CollectorDetailViewModel by viewModels {
        CollectorDetailViewModel.Factory(
            requireArguments().getInt(ARG_COLLECTOR_ID),
            CollectorRepository.create(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireArguments().getString(ARG_COLLECTOR_NAME)?.let { binding.toolbarCollectorDetail.title = it }
        binding.toolbarCollectorDetail.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.btnCollectorRetry.setOnClickListener { viewModel.loadCollector() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progressCollectorDetail.isVisible = state is CollectorDetailState.Loading
            binding.scrollCollectorDetail.isVisible = state is CollectorDetailState.Content
            binding.layoutCollectorError.isVisible = state is CollectorDetailState.Error
            if (state is CollectorDetailState.Content) showCollector(state.detail)
        }
    }

    private fun showCollector(detail: CollectorDetail) {
        val collector = detail.collector
        binding.toolbarCollectorDetail.title = collector.name
        binding.tvCollectorDetailName.text = collector.name
        binding.tvCollectorDetailInitials.text = CollectorText.initials(collector.name)

        val albumCount = detail.albums.size
        binding.chipCollectorAlbumsCount.text =
            resources.getQuantityString(R.plurals.collector_albums_count, albumCount, albumCount)

        binding.btnCollectorPhone.showIfNotBlank(collector.telephone)
        binding.btnCollectorPhone.contentDescription =
            getString(R.string.collector_call_description, collector.name, collector.telephone)
        binding.btnCollectorPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${collector.telephone}"))
            startActivity(intent)
        }

        binding.btnCollectorEmail.showIfNotBlank(collector.email)
        binding.btnCollectorEmail.contentDescription =
            getString(R.string.collector_email_description, collector.name, collector.email)
        binding.btnCollectorEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${collector.email}"))
            startActivity(intent)
        }

        showFavoritePerformers(collector.favoritePerformers)
        showCollection(detail.albums)
        showComments(collector.comments)
    }

    private fun showFavoritePerformers(performers: List<Performer>) {
        binding.tvCollectorPerformersEmpty.isVisible = performers.isEmpty()
        binding.rvCollectorPerformers.isVisible = performers.isNotEmpty()
        val adapter = CollectorPerformerAdapter(::openArtistDetail)
        binding.rvCollectorPerformers.adapter = adapter
        adapter.submitList(performers)
    }

    private fun openArtistDetail(performer: Performer) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.collectorDetailFragment) return
        navController.navigate(
            R.id.action_collectorDetailFragment_to_artistDetailFragment,
            bundleOf(
                ArtistDetailFragment.ARG_ARTIST_ID to performer.id,
                ArtistDetailFragment.ARG_ARTIST_TYPE to performer.type?.name,
                ArtistDetailFragment.ARG_ARTIST_NAME to performer.name
            )
        )
    }

    private fun showCollection(collectorAlbums: List<CollectorAlbum>) {
        binding.tvCollectorAlbumsHeader.text =
            getString(R.string.collector_albums_header_count, collectorAlbums.size)
        binding.tvCollectorAlbumsEmpty.isVisible = collectorAlbums.isEmpty()
        binding.layoutCollectorAlbums.isVisible = collectorAlbums.isNotEmpty()
        binding.layoutCollectorAlbums.removeAllViews()

        collectorAlbums.forEach { item ->
            val albumBinding = ItemCollectorAlbumBinding.inflate(layoutInflater, binding.layoutCollectorAlbums, true)
            albumBinding.tvCollectorAlbumName.text = item.album.name
            albumBinding.tvCollectorAlbumArtist.text = AlbumCardText.artists(item.album)
            albumBinding.tvCollectorAlbumPrice.text = getString(R.string.collector_album_price, item.price.toString())
            albumBinding.chipCollectorAlbumStatus.text = if (item.isAvailable) getString(R.string.collector_album_available) else getString(R.string.collector_album_unavailable)
            albumBinding.tvCollectorAlbumYear.text = ArtistDetailText.year(item.album.releaseDate)

            Glide.with(albumBinding.ivCollectorAlbumCover)
                .load(item.album.cover.ifBlank { null })
                .placeholder(R.drawable.ic_album_placeholder)
                .error(R.drawable.ic_album_placeholder)
                .fallback(R.drawable.ic_album_placeholder)
                .into(albumBinding.ivCollectorAlbumCover)

            albumBinding.root.setOnClickListener { openAlbumDetail(item.album.id, item.album.name) }
        }
    }

    private fun openAlbumDetail(albumId: Int, albumName: String) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.collectorDetailFragment) return
        navController.navigate(
            R.id.action_collectorDetailFragment_to_albumDetailFragment,
            bundleOf(
                AlbumDetailFragment.ARG_ALBUM_ID to albumId,
                AlbumDetailFragment.ARG_ALBUM_NAME to albumName
            )
        )
    }

    private fun showComments(comments: List<Comment>) {
        binding.tvCollectorCommentsHeader.text =
            getString(R.string.collector_comments_header_count, comments.size)
        binding.tvCollectorCommentsEmpty.isVisible = comments.isEmpty()
        binding.layoutCollectorComments.isVisible = comments.isNotEmpty()
        binding.layoutCollectorComments.removeAllViews()

        comments.forEach { comment ->
            val commentBinding = ItemCommentBinding.inflate(layoutInflater, binding.layoutCollectorComments, true)
            commentBinding.tvCommentAuthor.text = comment.author ?: getString(R.string.album_comment_unknown_author)
            commentBinding.ratingComment.rating = comment.rating.toFloat()
            commentBinding.ratingComment.contentDescription = getString(R.string.album_comment_rating, comment.rating)
            commentBinding.tvCommentText.text = comment.description
        }
    }

    private fun View.showIfNotBlank(value: String?) {
        isVisible = !value.isNullOrBlank()
        if (this is TextView) text = value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCollectorPerformers.adapter = null
        _binding = null
    }

    private class CollectorPerformerAdapter(
        private val onPerformerClick: (Performer) -> Unit
    ) : ListAdapter<Performer, CollectorPerformerAdapter.ViewHolder>(PerformerDiff) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemCollectorPerformerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(private val binding: ItemCollectorPerformerBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(performer: Performer) {
                binding.tvCollectorPerformerName.text = performer.name
                Glide.with(binding.ivCollectorPerformerImage)
                    .load(performer.image.ifBlank { null })
                    .placeholder(R.drawable.ic_artist_placeholder)
                    .error(R.drawable.ic_artist_placeholder)
                    .fallback(R.drawable.ic_artist_placeholder)
                    .into(binding.ivCollectorPerformerImage)

                binding.root.setOnClickListener { onPerformerClick(performer) }
            }
        }

        private object PerformerDiff : DiffUtil.ItemCallback<Performer>() {
            override fun areItemsTheSame(oldItem: Performer, newItem: Performer) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Performer, newItem: Performer) = oldItem == newItem
        }
    }

    companion object {
        const val ARG_COLLECTOR_ID = "collectorId"
        const val ARG_COLLECTOR_NAME = "collectorName"
    }
}
