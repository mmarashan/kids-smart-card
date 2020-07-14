package ru.volgadev.appsample

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import ru.volgadev.common.log.Logger
import ru.volgadev.sampledata.api.ArticleBackendApi
import ru.volgadev.sampledata.api.ArticleBackendApiImpl
import ru.volgadev.sampledata.repository.SampleRepository
import ru.volgadev.sampledata.repository.SampleRepositoryImpl
import ru.volgadev.samplefeature.ui.SampleViewModel

class SampleApplication : Application() {

    private val logger = Logger.get("SampleApplication")

    private val sampleModule = module {
        single<SampleRepository> {
            SampleRepositoryImpl.getInstance(
                context = get(),
                articleBackendApi = get()
            )
        }
        single<ArticleBackendApi> { ArticleBackendApiImpl() }
        viewModel {
            SampleViewModel(
                sampleRepository = get()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        logger.debug("on create")

        val koin = startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@SampleApplication)
            modules(sampleModule)
        }.koin

    }
}