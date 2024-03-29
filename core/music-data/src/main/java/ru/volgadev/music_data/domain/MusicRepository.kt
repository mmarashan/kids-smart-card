package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.volgadev.music_data.domain.model.MusicTrack

@WorkerThread
interface MusicRepository {

    fun musicTracks(): Flow<ArrayList<MusicTrack>>

    fun cardsAudios(): Flow<ArrayList<MusicTrack>>

    suspend fun loadAudio(url: String): MusicTrack?

    suspend fun getTrackFromStorage(url: String): MusicTrack?
}