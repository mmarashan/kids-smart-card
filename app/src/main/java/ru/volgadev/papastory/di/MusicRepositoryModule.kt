package ru.volgadev.papastory.di

import android.content.Context
import dagger.Binds
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
        fun providesMusicRepository(context: Context, musicBackendApi: MusicBackendApi): MusicRepository =
            MusicRepositoryImpl.getInstance(
                context = context,
                musicBackendApi = musicBackendApi
            )
    }

    @Binds
    fun bindsMusicBackendApi(impl: MusicBackendApiImpl): MusicBackendApi
}