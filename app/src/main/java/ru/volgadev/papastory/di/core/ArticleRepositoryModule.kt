package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.OkHttpClient
import ru.volgadev.article_repository.data.database.ArticleDatabaseProvider
import ru.volgadev.article_repository.data.datasource.ArticleRemoteDataSourceImpl
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.data.ArticleRepositoryImpl
import ru.volgadev.article_repository.data.database.ArticleDatabase
import ru.volgadev.article_repository.data.datasource.ArticleRemoteDataSource
import ru.volgadev.common.BACKEND_URL

@Module(
    includes = [PaymentManagerModule::class]
)
interface ArticleRepositoryModule {

    companion object {
        @Provides
        fun providesArticleDatabase(context: Context): ArticleDatabase =
            ArticleDatabaseProvider.createArticleDatabase(context)

        @Provides
        fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        fun providesOkHttpClient(): OkHttpClient = OkHttpClient()

        @Provides
        fun providesBaseUrl(): String = BACKEND_URL
    }

    @Binds
    fun bindsArticleRemoteDataSource(api: ArticleRemoteDataSourceImpl): ArticleRemoteDataSource

    @InternalCoroutinesApi
    @Binds
    fun bindsArticleRepository(articleRepository: ArticleRepositoryImpl): ArticleRepository
}