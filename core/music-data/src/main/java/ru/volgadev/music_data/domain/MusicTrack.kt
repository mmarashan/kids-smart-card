package ru.volgadev.music_data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MusicTrack(
    @PrimaryKey
    var url: String,
    var filePath: String?,
    var type: MusicTrackType = MusicTrackType.MUSIC
)

enum class MusicTrackType{
    MUSIC, ARTICLE_AUDIO
}