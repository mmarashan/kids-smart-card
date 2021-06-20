package ru.volgadev.papastory

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.volgadev.cardgallery.di.articleGalleryFeatureModule
import ru.volgadev.cardrepository.data.di.cardRepositoryModule
import ru.volgadev.cabinet_feature.di.cabinetFeatureModule
import ru.volgadev.common.log.AndroidLoggerDelegate
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.di.musicRepositoryModule
import ru.volgadev.papastory.di.appModule

class KidsCardApplication : Application() {

    private val logger = Logger.get("PapaStoryApplication")

    init {
        Logger.setDelegates(AndroidLoggerDelegate())
    }

    override fun onCreate() {
        super.onCreate()
        logger.debug("onCreate()")

        startKoin {
            androidLogger()
            androidContext(this@KidsCardApplication)
            modules(
                appModule,
                cardRepositoryModule,
                musicRepositoryModule,
                articleGalleryFeatureModule,
                cabinetFeatureModule
            )
        }
    }
}