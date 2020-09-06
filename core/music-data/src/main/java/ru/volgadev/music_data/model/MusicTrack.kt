package ru.volgadev.music_data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MusicTrack(
    @PrimaryKey
    val url: String,
    val filePath: String?
)