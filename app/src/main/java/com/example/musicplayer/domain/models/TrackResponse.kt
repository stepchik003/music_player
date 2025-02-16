package com.example.musicplayer.domain.models

data class TrackResponse(
    val id: Long,
    val title: String,
    val duration: Int,
    val preview: String,
    val artist: Artist,
    val album: Album,
) {
    fun toTrack() = Track(
        id = id,
        title = title,
        duration = duration,
        preview = preview,
        artistName = artist.name,
        artistPic = artist.pictureSmall,
        album = album.title,
        albumPicSmall = album.coverSmall,
        albumPicBig = album.coverBig
    )
}
