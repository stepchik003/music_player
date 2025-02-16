package com.example.musicplayer.domain.repository

import com.example.musicplayer.domain.models.Track

interface TrackRepository {
    suspend fun getApiTracks(): List<Track>
    suspend fun searchTracks(query: String): List<Track>
    fun getLocalTracks(): List<Track>
    fun searchLocalTracks(query: String): List<Track>
}