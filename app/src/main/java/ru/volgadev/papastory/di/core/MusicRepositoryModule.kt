package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.music_data.data.MusicBackendApiImpl
import ru.volgadev.music_data.data.MusicDatabaseProvider
import ru.volgadev.music_data.domain.MusicRepositoryImpl
import ru.volgadev.music_data.domain.MusicBackendApi
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.MusicTrackDatabase

@Module
interface MusicRepositoryModule {
    companion object {
        @Provides
        fun providesMusicTrackDatabase(context: Context): MusicTrackDatabase =
            MusicDatabaseProvider.createDatabase(context)
    }

    @Binds
    fun bindsMusicBackendApi(api: MusicBackendApiImpl): MusicBackendApi

    @InternalCoroutinesApi
    @Binds
    fun bindsMusicRepository(repository: MusicRepositoryImpl): MusicRepository
}