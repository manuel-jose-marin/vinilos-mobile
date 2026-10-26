package co.edu.uniandes.vinilos.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentAlbumDetailBinding
import co.edu.uniandes.vinilos.databinding.ItemCommentBinding
import co.edu.uniandes.vinilos.databinding.ItemTrackBinding
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Comment
import co.edu.uniandes.vinilos.models.Track
import co.edu.uniandes.vinilos.repositories.AlbumRepository
import co.edu.uniandes.vinilos.viewmodels.AlbumDetailState
import co.edu.uniandes.vinilos.viewmodels.AlbumDetailViewModel
import com.bumptech.glide.Glide

/** Pantalla "Detalle del álbum" (HU02): ficha técnica, tracks y comentarios, con estados de carga y error. */
class AlbumDetailFragment : Fragment() {

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlbumDetailViewModel by viewModels {
        AlbumDetailViewModel.Factory(
            requireArguments().getInt(ARG_ALBUM_ID),
            AlbumRepository.create(requireContext())
        )
    }

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
        binding.btnDetailRetry.setOnClickListener { viewModel.loadAlbum() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progressAlbumDetail.isVisible = state is AlbumDetailState.Loading
            binding.scrollAlbumDetail.isVisible = state is AlbumDetailState.Content
            binding.layoutDetailError.isVisible = state is AlbumDetailState.Error
            if (state is AlbumDetailState.Content) showAlbum(state.album)
        }
    }

    private fun showAlbum(album: Album) {
        binding.toolbarAlbumDetail.title = album.name
        binding.tvDetailName.text = album.name
        binding.tvDetailArtist.text =
            AlbumCardText.artists(album).ifEmpty { getString(R.string.album_unknown_artist) }
        Glide.with(binding.ivDetailCover)
            .load(album.cover.ifBlank { null })
            .placeholder(R.drawable.ic_album_placeholder)
            .error(R.drawable.ic_album_placeholder)
            .fallback(R.drawable.ic_album_placeholder)
            .into(binding.ivDetailCover)

        binding.chipReleaseDate.showIfNotBlank(AlbumDetailText.releaseDate(album.releaseDate))
        binding.chipGenre.showIfNotBlank(album.genre)
        binding.chipRecordLabel.showIfNotBlank(album.recordLabel)
        binding.tvDetailDescription.showIfNotBlank(album.description)

        showTracks(album.tracks)
        showComments(album.comments)
    }

    private fun showTracks(tracks: List<Track>) {
        binding.tvTracksHeader.text = getString(R.string.album_tracks_header, tracks.size)
        binding.tvTracksEmpty.isVisible = tracks.isEmpty()
        binding.layoutTracks.removeAllViews()
        tracks.forEachIndexed { index, track ->
            val item = ItemTrackBinding.inflate(layoutInflater, binding.layoutTracks, true)
            item.tvTrackNumber.text = (index + 1).toString()
            item.tvTrackName.text = track.name
            item.tvTrackDuration.text = track.duration
        }
    }

    private fun showComments(comments: List<Comment>) {
        binding.tvCommentsHeader.text = getString(R.string.album_comments_header, comments.size)
        binding.tvCommentsEmpty.isVisible = comments.isEmpty()
        binding.layoutComments.removeAllViews()
        comments.forEach { comment ->
            val item = ItemCommentBinding.inflate(layoutInflater, binding.layoutComments, true)
            item.tvCommentAuthor.text = comment.author ?: getString(R.string.album_comment_unknown_author)
            item.ratingComment.rating = comment.rating.toFloat()
            item.ratingComment.contentDescription = getString(R.string.album_comment_rating, comment.rating)
            item.tvCommentText.text = comment.description
        }
    }

    /** Muestra el texto, u oculta la vista si viene vacío. */
    private fun TextView.showIfNotBlank(value: String?) {
        isVisible = !value.isNullOrBlank()
        text = value
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
