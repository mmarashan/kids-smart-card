package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import ru.volgadev.music_data.api.MusicRepositoryComponentHolder
import ru.volgadev.music_data.api.MusicRepositoryDependencies
import ru.volgadev.music_data.api.MusicRepositoryApi

@Module
interface MusicRepositoryModule {
    companion object {
        @Provides
        fun providesMusicRepositoryDependencies(
            context: Context
        ): MusicRepositoryDependencies = MusicRepositoryDependencies(context)

        @Provides
        fun providesMusicRepositoryComponentHolder(
            musicRepositoryDependencies: MusicRepositoryDependencies
        ): MusicRepositoryComponentHolder = MusicRepositoryComponentHolder().apply {
            init(musicRepositoryDependencies)
        }

        @Reusable
        @Provides
        fun providesMusicRepositoryApi(musicRepositoryComponentHolder: MusicRepositoryComponentHolder): MusicRepositoryApi =
            musicRepositoryComponentHolder.get()
    }
}