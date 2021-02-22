package ru.volgadev.papastory

import android.app.Application
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import ru.volgadev.article_data.api.ArticleBackendApi
import ru.volgadev.article_data.api.ArticleBackendApiImpl
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.article_data.repository.ArticleRepositoryImpl
import ru.volgadev.article_galery.presentation.ArticleGalleryViewModel
import ru.volgadev.article_page.ArticlePageViewModel
import ru.volgadev.cabinet_feature.CabinetViewModel
import ru.volgadev.common.log.AndroidLoggerDelegate
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.api.MusicBackendApi
import ru.volgadev.music_data.api.MusicBackendApiImpl
import ru.volgadev.music_data.repository.MusicRepository
import ru.volgadev.music_data.repository.MusicRepositoryImpl
import ru.volgadev.papastory.di.ApplicationComponent
import ru.volgadev.papastory.di.DaggerApplicationComponent
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentManagerFactory

class KidsCardApplication : Application() {

    private val logger = Logger.get("PapaStoryApplication")

    init {
        Logger.setDelegates(AndroidLoggerDelegate())
    }

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        logger.debug("onCreate()")
        appComponent = DaggerApplicationComponent.factory().create(applicationContext)
    }
}