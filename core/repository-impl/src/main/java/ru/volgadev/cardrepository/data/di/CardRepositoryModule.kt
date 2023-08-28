package ru.volgadev.cardrepository.data.di

import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import ru.volgadev.cardrepository.data.CardRepositoryImpl
import ru.volgadev.cardrepository.data.database.CardDatabase
import ru.volgadev.cardrepository.data.datasource.CardRemoteDataSource
import ru.volgadev.cardrepository.data.datasource.CardRemoteDataSourceImpl
import ru.volgadev.cardrepository.domain.CardRepository
import ru.volgadev.core.settings.api.Settings
import ru.volgadev.googlebillingclientwrapper.api.PaymentManager
import ru.volgadev.googlebillingclientwrapper.api.PaymentManagerFactory

/**
 * DI module of repository implementation
 */
val cardRepositoryModule = module {

    single<CardRepository> {
        CardRepositoryImpl(
            remoteDataSource = get(),
            paymentManager = get(),
            database = get(),
            ioDispatcher = Dispatchers.IO
        )
    }
    single<CardRemoteDataSource> {
        CardRemoteDataSourceImpl(
            baseUrl = get<Settings>().baseUrl(),
            client = get()
        )
    }
    single<PaymentManager> { PaymentManagerFactory.createPaymentManager(get()) }
    single<CardDatabase> { CardDatabase.create(get()) }
}