package ru.volgadev.appsample

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import ru.volgadev.appsample.data.repository.Repositories
import ru.volgadev.common.log.Logger
import ru.volgadev.samplefeature.data.repository.SampleRepository
import ru.volgadev.samplefeature.main.SampleViewModel

class SampleApplication : Application() {

    private val logger = Logger.get("SampleApplication")

    private val sampleModule = module {
        single<SampleRepository> { Repositories.getSampleRepository(context = get()) }
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