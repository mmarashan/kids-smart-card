package ru.volgadev.papastory.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.volgadev.music_data.api.MusicBackendApi
import ru.volgadev.music_data.api.MusicBackendApiImpl
import ru.volgadev.music_data.repository.MusicRepository
import ru.volgadev.music_data.repository.MusicRepositoryImpl

@Module
interface MusicRepositoryModule {
    companion object {
        @Provides
        fun getMusicRepository(context: Context, musicBackendApi: MusicBackendApi): MusicRepository =
            MusicRepositoryImpl.getInstance(
                context = context,
                musicBackendApi = musicBackendApi
            )

        @Provides
        fun getMusicBackendApi(): MusicBackendApi = MusicBackendApiImpl()
    }
}