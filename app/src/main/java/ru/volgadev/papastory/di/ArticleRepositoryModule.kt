package ru.volgadev.papastory.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import ru.volgadev.article_data.api.ArticleBackendApi
import ru.volgadev.article_data.api.ArticleBackendApiImpl
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.article_data.repository.ArticleRepositoryImpl
import ru.volgadev.article_data.storage.ArticleCategoriesDatabase
import ru.volgadev.article_data.storage.ArticleDatabase
import ru.volgadev.article_data.storage.LocalStorageProvider

@Module
interface ArticleRepositoryModule {

    companion object {
        @Provides
        fun getArticleDatabase(context: Context): ArticleDatabase = LocalStorageProvider.getArticleDatabase(context)

        @Provides
        fun getArticleCategoriesDatabase(context: Context): ArticleCategoriesDatabase =
            LocalStorageProvider.getArticleCategoriesDatabase(context)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Binds
    fun bindsArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository

    @Binds
    fun bindsArticleBackendApi(impl: ArticleBackendApiImpl): ArticleBackendApi
}