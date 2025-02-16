package com.example.musicplayer.domain.models

import com.google.gson.annotations.SerializedName

data class Artist(
    val id: Long,
    val name: String,
    @SerializedName("picture_small")
    val pictureSmall: String
)
