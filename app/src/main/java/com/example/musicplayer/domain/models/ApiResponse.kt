package com.example.musicplayer.domain.models


data class ApiResponse(
    val tracks: TracksResponse
)

data class TracksResponse(
    val data: List<TrackResponse>?
)
