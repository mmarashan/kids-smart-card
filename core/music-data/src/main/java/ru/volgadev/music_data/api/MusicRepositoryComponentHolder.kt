package ru.volgadev.music_data.api

import android.content.Context
import ru.sberdevices.module_injector.BaseDependencies
import ru.sberdevices.module_injector.ComponentHolder
import ru.volgadev.music_data.data.MusicBackendApiImpl
import ru.volgadev.music_data.data.MusicTrackDatabaseImpl

class MusicRepositoryDependencies(val context: Context) : BaseDependencies

class MusicRepositoryComponentHolder : ComponentHolder<MusicRepositoryApi, MusicRepositoryDependencies> {

    private var musicRepositoryApi: MusicRepositoryApi? = null
    private var dependencies: MusicRepositoryDependencies? = null

    override fun init(dependencies: MusicRepositoryDependencies) {
        this.dependencies = dependencies
    }

    override fun get(): MusicRepositoryApi {
        val dependencies = dependencies
        checkNotNull(dependencies)
        var musicRepositoryApiImpl = musicRepositoryApi
        if (musicRepositoryApiImpl == null) {
            musicRepositoryApiImpl = MusicRepositoryApiImpl(
                context = dependencies.context,
                musicBackendApi = MusicBackendApiImpl(),
                musicTrackDatabase = MusicTrackDatabaseImpl.getInstance(dependencies.context)
            )
            this.musicRepositoryApi = musicRepositoryApiImpl
            return musicRepositoryApiImpl
        } else {
            return musicRepositoryApiImpl
        }
    }

    override fun reset() {
        musicRepositoryApi = null
        dependencies = null
    }
}