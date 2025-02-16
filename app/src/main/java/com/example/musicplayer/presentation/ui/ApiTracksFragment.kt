package com.example.musicplayer.presentation.ui

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.domain.models.Track
import com.example.musicplayer.presentation.viewmodels.ApiTracksViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ApiTracksFragment : BaseTrackFragment() {
    override val viewModel: ApiTracksViewModel by viewModels()

    override fun loadInitialData() {
        viewModel.loadTracks()
    }

    override fun onTrackClicked(track: Track) {
        val bundle = Bundle().apply {
            val tracks = viewModel.tracks.value
            putParcelableArrayList("tracks", ArrayList(tracks))
            putInt("currentPosition", tracks.indexOf(track))
        }
        findNavController().navigate(R.id.action_to_player, bundle)
    }
}