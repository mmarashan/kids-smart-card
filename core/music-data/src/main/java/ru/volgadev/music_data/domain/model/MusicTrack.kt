package ru.volgadev.music_data.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MusicTrack(
    @PrimaryKey
    var url: String,
    var filePath: String?,
    var type: MusicTrackType
)

enum class MusicTrackType{
    MUSIC, ARTICLE_AUDIO
}