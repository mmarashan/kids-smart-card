package ru.volgadev.music_data.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.volgadev.music_data.model.MusicTrack

@WorkerThread
interface MusicRepository {

    fun musicTracks(): Flow<ArrayList<MusicTrack>>
}