package com.example.musicplayer.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val id: Long,
    val title: String,
    val duration: Int,
    val preview: String,
    val artistName: String,
    val artistPic: String? = null,
    val album: String?,
    val albumPicSmall: String? = null,
    val albumPicBig: String? = null,
    val isLocal: Boolean = false
) : Parcelable
