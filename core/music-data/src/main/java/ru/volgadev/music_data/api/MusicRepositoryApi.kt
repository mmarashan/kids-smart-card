package ru.volgadev.music_data.api

import android.content.Context
import ru.sberdevices.module_injector.BaseAPI
import ru.volgadev.music_data.data.MusicRepositoryImpl
import ru.volgadev.music_data.domain.MusicBackendApi
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.MusicTrackDatabase

interface MusicRepositoryApi : BaseAPI {
    fun getMusicRepository(): MusicRepository
}

internal class MusicRepositoryApiImpl(
    private val context: Context,
    private val musicBackendApi: MusicBackendApi,
    private val musicTrackDatabase: MusicTrackDatabase
) : MusicRepositoryApi {

    override fun getMusicRepository(): MusicRepository = MusicRepositoryImpl(
        context = context,
        musicBackendApi = musicBackendApi,
        musicTrackDatabase = musicTrackDatabase
    )
}