package com.example.musicplayer.data

import com.example.musicplayer.domain.models.ApiResponse
import com.example.musicplayer.domain.models.TracksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackApiService {
    @GET("chart")
    suspend fun getChartTracks(): Response<ApiResponse>

    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): Response<TracksResponse>
}