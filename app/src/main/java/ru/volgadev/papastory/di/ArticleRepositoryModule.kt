package ru.volgadev.papastory.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.api.ArticleRepositoryApi
import ru.volgadev.article_data.api.ArticleRepositoryComponentHolder
import ru.volgadev.article_data.api.ArticleRepositoryDependencies
import ru.volgadev.pay_lib.PaymentManager

@Module(
    includes = [PaymentManagerModule::class]
)
interface ArticleRepositoryModule {

    companion object {
        @Provides
        fun providesArticleRepositoryDependencies(
            context: Context,
            paymentManager: PaymentManager
        ): ArticleRepositoryDependencies = ArticleRepositoryDependencies(context, paymentManager)

        @Provides
        fun providesArticleRepositoryComponentHolder(
            articleRepositoryDependencies: ArticleRepositoryDependencies
        ): ArticleRepositoryComponentHolder = ArticleRepositoryComponentHolder().apply {
            init(articleRepositoryDependencies)
        }

        @Provides
        fun providesArticleRepositoryApi(articleRepositoryComponentHolder: ArticleRepositoryComponentHolder): ArticleRepositoryApi =
            articleRepositoryComponentHolder.get()
    }
}