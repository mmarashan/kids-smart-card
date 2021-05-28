package ru.volgadev.article_repository.data.di

import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.koin.dsl.module
import ru.volgadev.article_repository.data.ArticleRepositoryImpl
import ru.volgadev.article_repository.data.database.ArticleDatabase
import ru.volgadev.article_repository.data.database.ArticleDatabaseProvider
import ru.volgadev.article_repository.data.datasource.ArticleRemoteDataSource
import ru.volgadev.article_repository.data.datasource.ArticleRemoteDataSourceImpl
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.common.BACKEND_URL
import ru.volgadev.googlebillingclientwrapper.api.PaymentManager
import ru.volgadev.googlebillingclientwrapper.api.PaymentManagerFactory

/**
 * DI module of repository implementation
 */
val articleRepositoryModule = module {

    single<ArticleRepository> {
        ArticleRepositoryImpl(
            remoteDataSource = get(),
            paymentManager = get(),
            database = get(),
            ioDispatcher = Dispatchers.IO
        )
    }
    single<ArticleRemoteDataSource> {
        ArticleRemoteDataSourceImpl(
            baseUrl = BACKEND_URL,
            client = get()
        )
    }
    single<PaymentManager> { PaymentManagerFactory.createPaymentManager(get()) }
    single<ArticleDatabase> { ArticleDatabaseProvider.createArticleDatabase(get()) }
    single<OkHttpClient> { OkHttpClient() }
}