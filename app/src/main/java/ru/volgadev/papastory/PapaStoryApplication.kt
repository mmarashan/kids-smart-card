package ru.volgadev.papastory

import android.app.Application
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
import ru.volgadev.article_galery.ui.ArticleGalleryViewModel
import ru.volgadev.article_page.ArticlePageViewModel
import ru.volgadev.common.log.AndroidLoggerDelegate
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.api.MusicBackendApi
import ru.volgadev.music_data.api.MusicBackendApiImpl
import ru.volgadev.music_data.repository.MusicRepository
import ru.volgadev.music_data.repository.MusicRepositoryImpl

class PapaStoryApplication : Application() {

    private val logger = Logger.get("PapaStoryApplication")

    init {
        Logger.setDelegates(AndroidLoggerDelegate())
    }

    private val sampleModule = module {
        single<ArticleRepository> {
            ArticleRepositoryImpl.getInstance(
                context = get(),
                articleBackendApi = get()
            )
        }
        single<ArticleBackendApi> { ArticleBackendApiImpl() }
        single<MusicRepository> {
            MusicRepositoryImpl.getInstance(
                context = get(),
                musicBackendApi = get()
            )
        }
        single<MusicBackendApi> { MusicBackendApiImpl() }
        viewModel {
            ArticleGalleryViewModel(get(), get())
        }
        viewModel {
            ArticlePageViewModel(get(), get())
        }
    }

    override fun onCreate() {
        super.onCreate()
        logger.debug("on create")

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@PapaStoryApplication)
            modules(sampleModule)
        }.koin

    }
}