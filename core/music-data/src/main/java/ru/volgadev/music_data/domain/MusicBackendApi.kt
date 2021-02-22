package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread

@WorkerThread
internal interface MusicBackendApi {
    fun getTracks(): List<MusicTrack>
}