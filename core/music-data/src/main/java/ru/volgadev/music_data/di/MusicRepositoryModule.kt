package ru.volgadev.music_data.di

import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.koin.dsl.module
import ru.volgadev.music_data.data.MusicBackendApiImpl
import ru.volgadev.music_data.data.MusicDatabaseProvider
import ru.volgadev.music_data.domain.MusicBackendApi
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.MusicRepositoryImpl
import ru.volgadev.music_data.domain.MusicTrackDatabase

val musicRepositoryModule = module {
    single<MusicRepository> {
        MusicRepositoryImpl(
            context = get(),
            musicBackendApi = get(),
            musicTrackDatabase = get(),
            ioDispatcher = Dispatchers.IO
        )
    }

    single<MusicBackendApi> { MusicBackendApiImpl(client = get()) }
    single<MusicTrackDatabase> { MusicDatabaseProvider.createDatabase(get()) }
}