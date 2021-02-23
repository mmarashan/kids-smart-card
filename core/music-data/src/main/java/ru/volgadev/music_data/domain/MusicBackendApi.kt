package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread

@WorkerThread
interface MusicBackendApi {
    fun getTracks(): List<MusicTrack>
}