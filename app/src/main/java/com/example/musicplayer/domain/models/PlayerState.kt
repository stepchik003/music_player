package com.example.musicplayer.domain.models

data class PlayerState(
    val track: Track,
    val isPlaying: Boolean,
    val currentPosition: Int,
    val duration: Int
)