package com.example.musicplayer.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiTracksViewModel @Inject constructor(
    private val repository: TrackRepository
) : BaseTrackViewModel() {

    override fun loadTracks() {
        viewModelScope.launch {
            _tracks.value = repository.getApiTracks()
        }
    }

    override fun searchTracks(query: String) {
        viewModelScope.launch {
            _tracks.value = repository.searchTracks(query)
        }
    }
}