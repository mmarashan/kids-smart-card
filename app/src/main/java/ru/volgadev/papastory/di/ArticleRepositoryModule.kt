package ru.volgadev.papastory.di

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.article_data.api.ArticleBackendApi
import ru.volgadev.article_data.api.ArticleBackendApiImpl
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.article_data.repository.ArticleRepositoryImpl
import ru.volgadev.pay_lib.PaymentManager

@Module
interface ArticleRepositoryModule {
    companion object {
        @ExperimentalCoroutinesApi
        @InternalCoroutinesApi
        @Provides
        fun getArticleRepository(
            context: Context,
            articleBackendApi: ArticleBackendApi,
            paymentManager: PaymentManager
        ): ArticleRepository {
            return ArticleRepositoryImpl(context, articleBackendApi, paymentManager)
        }

        fun getArticleBackendApi(): ArticleBackendApi {
            return ArticleBackendApiImpl()
        }
    }
}