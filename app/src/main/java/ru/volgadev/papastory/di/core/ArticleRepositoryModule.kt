package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.article_repository.data.database.ArticleDatabaseProvider
import ru.volgadev.article_repository.data.datasource.ArticleBackendApiImpl
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.ArticleRepositoryImpl
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.datasource.ArticleBackendApi

@Module(
    includes = [PaymentManagerModule::class]
)
interface ArticleRepositoryModule {

    companion object {
        @Provides
        fun providesArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseProvider.createArticleDatabase(context)
    }

    @Binds
    fun bindsArticleBackendApi(api: ArticleBackendApiImpl): ArticleBackendApi

    @InternalCoroutinesApi
    @Binds
    fun bindsArticleRepository(articleRepository: ArticleRepositoryImpl): ArticleRepository
}