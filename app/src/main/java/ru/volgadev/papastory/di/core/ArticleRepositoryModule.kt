package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.article_data.data.ArticleBackendApiImpl
import ru.volgadev.article_data.data.ArticleDatabaseProvider
import ru.volgadev.article_data.domain.ArticleBackendApi
import ru.volgadev.article_data.domain.ArticleCategoriesDatabase
import ru.volgadev.article_data.domain.ArticleDatabase
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.article_data.domain.ArticleRepositoryImpl

@Module(
    includes = [PaymentManagerModule::class]
)
interface ArticleRepositoryModule {

    companion object {
        @Provides
        fun providesArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseProvider.createArticleDatabase(context)

        @Provides
        fun providesArticleCategoriesDatabase(context: Context): ArticleCategoriesDatabase =
            ArticleDatabaseProvider.createArticleCategoriesDatabase(context)
    }

    @Binds
    fun bindsArticleBackendApi(api: ArticleBackendApiImpl): ArticleBackendApi

    @InternalCoroutinesApi
    @Binds
    fun bindsArticleRepository(articleRepository: ArticleRepositoryImpl): ArticleRepository
}