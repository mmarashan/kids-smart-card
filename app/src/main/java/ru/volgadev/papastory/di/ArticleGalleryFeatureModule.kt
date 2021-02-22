package ru.volgadev.papastory.di

import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.repository.ArticleRepository
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
            articleRepository: ArticleRepository,
            musicRepository: MusicRepository
        ): ArticleGalleryFeatureDependencies = ArticleGalleryFeatureDependencies(articleRepository, musicRepository)

        @Provides
        fun providesArticleGalleryFeatureComponentHolder(dependencies: ArticleGalleryFeatureDependencies): ArticleGalleryFeatureComponentHolder =
            ArticleGalleryFeatureComponentHolder().apply {
                init(dependencies)
            }

        @Provides
        fun providesArticleGalleryFeatureApi(holder: ArticleGalleryFeatureComponentHolder) = holder.get()
    }
}
