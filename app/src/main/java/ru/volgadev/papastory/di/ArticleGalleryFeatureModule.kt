package ru.volgadev.papastory.di

import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.api.ArticleRepositoryApi
import ru.volgadev.article_galery.api.ArticleGalleryFeatureComponentHolder
import ru.volgadev.article_galery.api.ArticleGalleryFeatureDependencies
import ru.volgadev.music_data.repository.MusicRepository

@Module(
    includes = [ArticleRepositoryModule::class, MusicRepositoryModule::class]
)
interface ArticleGalleryFeatureModule {

    companion object {
        @Provides
        fun providesArticleGalleryFeatureDependencies(
            articleRepositoryApi: ArticleRepositoryApi,
            musicRepository: MusicRepository
        ): ArticleGalleryFeatureDependencies =
            ArticleGalleryFeatureDependencies(articleRepositoryApi.getArticleRepository(), musicRepository)

        @Provides
        fun providesArticleGalleryFeatureComponentHolder(dependencies: ArticleGalleryFeatureDependencies): ArticleGalleryFeatureComponentHolder =
            ArticleGalleryFeatureComponentHolder().apply {
                init(dependencies)
            }

        @Provides
        fun providesArticleGalleryFeatureApi(holder: ArticleGalleryFeatureComponentHolder) = holder.get()
    }
}
