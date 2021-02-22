package ru.volgadev.music_data.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

@WorkerThread
interface MusicRepository {

    fun musicTracks(): Flow<ArrayList<MusicTrack>>

    fun articleAudios(): Flow<ArrayList<MusicTrack>>

    suspend fun loadArticleAudio(url: String): MusicTrack?

    suspend fun getTrackFromStorage(url: String): MusicTrack?
}