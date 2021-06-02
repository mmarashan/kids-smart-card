package ru.volgadev.papastory.di

import okhttp3.OkHttpClient
import org.koin.dsl.module

internal val appModule = module {

    factory<OkHttpClient> { OkHttpClient() }
}