package ru.volgadev.papastory.di

import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.api.ArticleRepositoryApi
import ru.volgadev.article_page.api.ArticlePageFeatureComponentHolder
import ru.volgadev.article_page.api.ArticlePageFeatureDependencies
import ru.volgadev.music_data.api.MusicRepositoryApi

@Module(
    includes = [ArticleRepositoryModule::class, MusicRepositoryModule::class]
)
interface ArticlePageFeatureModule {

    companion object {
        @Provides
        fun providesArticlePageFeatureDependencies(
            articleRepositoryApi: ArticleRepositoryApi,
            musicRepositoryApi: MusicRepositoryApi
        ): ArticlePageFeatureDependencies =
            ArticlePageFeatureDependencies(
                articleRepository = articleRepositoryApi.getArticleRepository(),
                musicRepository = musicRepositoryApi.getMusicRepository()
            )

        @Provides
        fun providesArticlePageFeatureComponentHolder(dependencies: ArticlePageFeatureDependencies): ArticlePageFeatureComponentHolder =
            ArticlePageFeatureComponentHolder().apply {
                init(dependencies)
            }

        @Provides
        fun providesArticlePageFeatureApi(holder: ArticlePageFeatureComponentHolder) = holder.get()
    }
}
