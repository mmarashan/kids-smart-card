package ru.volgadev.music_data.api

import androidx.annotation.WorkerThread
import ru.volgadev.music_data.model.MusicTrack

@WorkerThread
interface MusicBackendApi {
    fun getTracks(): List<MusicTrack>
}