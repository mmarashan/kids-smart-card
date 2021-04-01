package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread
import ru.volgadev.music_data.domain.model.MusicTrack

@WorkerThread
interface MusicBackendApi {
    fun getTracks(): List<MusicTrack>
}