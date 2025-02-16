package com.example.musicplayer.data

import com.example.musicplayer.domain.models.Track
import com.example.musicplayer.domain.repository.TrackRepository
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val apiService: TrackApiService,
    private val localDataSource: TrackLocalDataSource
) : TrackRepository {
    override suspend fun getApiTracks(): List<Track> {
        return try {
            val response = apiService.getChartTracks()
            response.body()?.tracks?.data?.map { it.toTrack() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            val response = apiService.searchTracks(query)
            response.body()?.data?.map { it.toTrack() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getLocalTracks(): List<Track> {
        val tracks = localDataSource.getLocalTracks()
        return tracks
    }

    override fun searchLocalTracks(query: String): List<Track> {
        return localDataSource.getLocalTracks().filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artistName.contains(query, ignoreCase = true)
        }
    }
}