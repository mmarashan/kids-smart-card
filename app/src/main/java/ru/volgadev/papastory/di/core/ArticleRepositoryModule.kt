package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.article_repository.data.datasource.ArticleBackendApiImpl
import ru.volgadev.article_repository.data.database.ArticleDatabaseProvider
import ru.volgadev.article_repository.domain.datasource.ArticleBackendApi
import ru.volgadev.article_repository.domain.database.ArticleCategoriesDatabase
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.ArticleRepositoryImpl

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