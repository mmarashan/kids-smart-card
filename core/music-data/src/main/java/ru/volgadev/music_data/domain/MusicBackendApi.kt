package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread
import ru.volgadev.music_data.domain.model.MusicTrack

@WorkerThread
internal interface MusicBackendApi {
    fun getTracks(): List<MusicTrack>
}