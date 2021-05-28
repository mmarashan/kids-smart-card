package ru.volgadev.papastory

import android.app.Application
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.volgadev.article_galery.di.articleGalleryFeatureModule
import ru.volgadev.article_repository.data.di.articleRepositoryModule
import ru.volgadev.cabinet_feature.di.cabinetFeatureModule
import ru.volgadev.common.log.AndroidLoggerDelegate
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.di.musicRepositoryModule

class KidsCardApplication : Application() {

    private val logger = Logger.get("PapaStoryApplication")

    init {
        Logger.setDelegates(AndroidLoggerDelegate())
    }

    val appModule = module {

        factory<OkHttpClient> { OkHttpClient() }
    }

    override fun onCreate() {
        super.onCreate()
        logger.debug("onCreate()")

        startKoin {
            androidLogger()
            androidContext(this@KidsCardApplication)
            modules(
                appModule,
                articleRepositoryModule,
                musicRepositoryModule,
                articleGalleryFeatureModule,
                cabinetFeatureModule
            )
        }
    }
}