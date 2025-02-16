package com.example.musicplayer.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.musicplayer.domain.models.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseTrackViewModel : ViewModel() {
    protected val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    abstract fun loadTracks()
    abstract fun searchTracks(query: String)
}