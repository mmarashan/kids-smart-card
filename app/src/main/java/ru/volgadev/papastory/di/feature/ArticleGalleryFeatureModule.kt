package ru.volgadev.papastory.di.feature

import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.api.ArticleRepositoryApi
import ru.volgadev.music_data.api.MusicRepositoryApi
import ru.volgadev.article_galery.api.ArticleGalleryFeatureComponentHolder
import ru.volgadev.article_galery.api.ArticleGalleryFeatureDependencies
import ru.volgadev.papastory.di.core.ArticleRepositoryModule
import ru.volgadev.papastory.di.core.MusicRepositoryModule

@Module(
    includes = [ArticleRepositoryModule::class, MusicRepositoryModule::class]
)
interface ArticleGalleryFeatureModule {

    companion object {
        @Provides
        fun providesArticleGalleryFeatureDependencies(
            articleRepositoryApi: ArticleRepositoryApi,
            musicRepositoryApi: MusicRepositoryApi
        ): ArticleGalleryFeatureDependencies =
            ArticleGalleryFeatureDependencies(
                articleRepository = articleRepositoryApi.getArticleRepository(),
                musicRepository = musicRepositoryApi.getMusicRepository()
            )

        @Provides
        fun providesArticleGalleryFeatureComponentHolder(dependencies: ArticleGalleryFeatureDependencies): ArticleGalleryFeatureComponentHolder =
            ArticleGalleryFeatureComponentHolder().apply {
                init(dependencies)
            }

        @Provides
        fun providesArticleGalleryFeatureApi(holder: ArticleGalleryFeatureComponentHolder) = holder.get()
    }
}
