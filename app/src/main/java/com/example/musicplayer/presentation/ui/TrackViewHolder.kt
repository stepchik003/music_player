package com.example.musicplayer.presentation.ui

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.R
import com.example.musicplayer.databinding.TrackItemBinding
import com.example.musicplayer.domain.models.Track

class TrackViewHolder(
    private val binding: TrackItemBinding,
    private val onItemClick: (Track) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(track: Track) {
        binding.title.text = track.title
        binding.artist.text = track.artistName

        Glide.with(binding.root)
            .load(track.albumPicSmall)
            .error(R.drawable.img_1)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(16)))
            .into(binding.cover)

        binding.root.setOnClickListener {
            onItemClick(track)
        }
    }
}