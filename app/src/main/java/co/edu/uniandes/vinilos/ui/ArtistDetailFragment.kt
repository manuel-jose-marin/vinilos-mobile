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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.uniandes.vinilos.R
import co.edu.uniandes.vinilos.databinding.FragmentArtistDetailBinding
import co.edu.uniandes.vinilos.databinding.ItemArtistAlbumBinding
import co.edu.uniandes.vinilos.databinding.ItemArtistMemberBinding
import co.edu.uniandes.vinilos.databinding.ItemArtistPrizeBinding
import co.edu.uniandes.vinilos.models.Album
import co.edu.uniandes.vinilos.models.Artist
import co.edu.uniandes.vinilos.models.ArtistPrize
import co.edu.uniandes.vinilos.models.ArtistType
import co.edu.uniandes.vinilos.repositories.ArtistRepository
import co.edu.uniandes.vinilos.viewmodels.ArtistDetailState
import co.edu.uniandes.vinilos.viewmodels.ArtistDetailViewModel
import com.bumptech.glide.Glide

/** Pantalla "Detalle del artista" (HU04): ficha técnica, carrusel de álbumes, premios e integrantes. */
class ArtistDetailFragment : Fragment() {

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistDetailViewModel by viewModels {
        val args = requireArguments()
        val artistId = args.getInt(ARG_ARTIST_ID)
        val typeName = args.getString(ARG_ARTIST_TYPE) ?: ArtistType.MUSICIAN.name
        val artistType = try { ArtistType.valueOf(typeName) } catch (e: Exception) { ArtistType.MUSICIAN }
        ArtistDetailViewModel.Factory(artistId, artistType, ArtistRepository.create(requireContext()))
    }

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
        binding.btnArtistRetry.setOnClickListener { viewModel.loadArtist() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progressArtistDetail.isVisible = state is ArtistDetailState.Loading
            binding.scrollArtistDetail.isVisible = state is ArtistDetailState.Content
            binding.layoutArtistError.isVisible = state is ArtistDetailState.Error
            if (state is ArtistDetailState.Content) showArtist(state.artist)
        }
    }

    private fun showArtist(artist: Artist) {
        binding.toolbarArtistDetail.title = artist.name
        binding.tvArtistName.text = artist.name

        binding.chipArtistType.setText(
            when (artist.type) {
                ArtistType.MUSICIAN -> R.string.artist_type_musician
                ArtistType.BAND -> R.string.artist_type_band
            }
        )

        binding.chipArtistDate.text = ArtistDetailText.formattedDate(artist)

        Glide.with(binding.ivArtistImage)
            .load(artist.image.ifBlank { null })
            .placeholder(R.drawable.ic_nav_artists)
            .error(R.drawable.ic_nav_artists)
            .fallback(R.drawable.ic_nav_artists)
            .into(binding.ivArtistImage)

        binding.tvArtistDescription.text = artist.description
        binding.tvArtistDescription.post {
            if (_binding == null) return@post
            val isEllipsized = binding.tvArtistDescription.layout?.let { layout ->
                layout.lineCount > 0 && layout.getEllipsisCount(layout.lineCount - 1) > 0
            } ?: false
            binding.btnArtistReadMore.isVisible = isEllipsized
        }

        binding.btnArtistReadMore.setOnClickListener {
            binding.tvArtistDescription.maxLines = Int.MAX_VALUE
            binding.btnArtistReadMore.isVisible = false
        }

        showAlbums(artist.albums)
        showPrizes(artist.prizes)
        showMembers(artist)
    }

    private fun showAlbums(albums: List<Album>) {
        binding.tvArtistAlbumsEmpty.isVisible = albums.isEmpty()
        binding.rvArtistAlbums.isVisible = albums.isNotEmpty()
        val adapter = ArtistAlbumAdapter(::openAlbumDetail)
        binding.rvArtistAlbums.adapter = adapter
        adapter.submitList(albums)
    }

    private fun openAlbumDetail(album: Album) {
        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.artistDetailFragment) return
        navController.navigate(
            R.id.action_artistDetailFragment_to_albumDetailFragment,
            bundleOf(
                AlbumDetailFragment.ARG_ALBUM_ID to album.id,
                AlbumDetailFragment.ARG_ALBUM_NAME to album.name
            )
        )
    }

    private fun showPrizes(prizes: List<ArtistPrize>) {
        binding.tvArtistPrizesEmpty.isVisible = prizes.isEmpty()
        binding.layoutArtistPrizes.isVisible = prizes.isNotEmpty()
        binding.layoutArtistPrizes.removeAllViews()
        prizes.forEach { prize ->
            val item = ItemArtistPrizeBinding.inflate(layoutInflater, binding.layoutArtistPrizes, true)
            item.tvPrizeName.text = prize.name ?: getString(R.string.artist_prizes_header)
            item.tvPrizeOrganization.text = prize.organization ?: ""
            item.tvPrizeYear.text = ArtistDetailText.year(prize.premiationDate)
        }
    }

    private fun showMembers(artist: Artist) {
        val isBand = artist.type == ArtistType.BAND
        binding.sectionArtistMembers.isVisible = isBand
        if (!isBand) return

        val members = artist.members
        binding.tvArtistMembersEmpty.isVisible = members.isEmpty()
        binding.layoutArtistMembers.removeAllViews()
        members.forEach { member ->
            val item = ItemArtistMemberBinding.inflate(layoutInflater, binding.layoutArtistMembers, true)
            item.tvMemberName.text = member.name
            Glide.with(item.ivMemberImage)
                .load(member.image.ifBlank { null })
                .placeholder(R.drawable.ic_album_placeholder)
                .error(R.drawable.ic_album_placeholder)
                .fallback(R.drawable.ic_album_placeholder)
                .into(item.ivMemberImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvArtistAlbums.adapter = null
        _binding = null
    }

    private class ArtistAlbumAdapter(
        private val onAlbumClick: (Album) -> Unit
    ) : ListAdapter<Album, ArtistAlbumAdapter.ViewHolder>(AlbumDiff) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemArtistAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class ViewHolder(private val binding: ItemArtistAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(album: Album) {
                binding.tvArtistAlbumName.text = album.name
                val year = ArtistDetailText.year(album.releaseDate)
                binding.tvArtistAlbumYearLabel.text = "$year · ${album.recordLabel}"

                Glide.with(binding.ivArtistAlbumCover)
                    .load(album.cover.ifBlank { null })
                    .placeholder(R.drawable.ic_album_placeholder)
                    .error(R.drawable.ic_album_placeholder)
                    .fallback(R.drawable.ic_album_placeholder)
                    .into(binding.ivArtistAlbumCover)

                binding.root.setOnClickListener { onAlbumClick(album) }
            }
        }

        private object AlbumDiff : DiffUtil.ItemCallback<Album>() {
            override fun areItemsTheSame(oldItem: Album, newItem: Album) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Album, newItem: Album) = oldItem == newItem
        }
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
