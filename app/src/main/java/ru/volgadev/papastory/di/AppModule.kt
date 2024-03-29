package ru.volgadev.papastory.di

import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.volgadev.core.settings.api.Settings
import ru.volgadev.papastory.presentation.MainViewModel

internal val appModule = module {

    factory<OkHttpClient> { OkHttpClient() }
    viewModel { MainViewModel() }
    single<Settings> { Settings.get() }
}