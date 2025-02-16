package com.example.musicplayer.domain.models

import com.google.gson.annotations.SerializedName

data class Album(
    val id: Long,
    val title: String,
    @SerializedName("cover_medium")
    val coverSmall: String,
    @SerializedName("cover_big")
    val coverBig: String
)
