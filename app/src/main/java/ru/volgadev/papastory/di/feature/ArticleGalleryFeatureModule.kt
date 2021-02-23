package ru.volgadev.papastory.di.feature

import dagger.Module
import dagger.Provides
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.article_galery.api.ArticleGalleryFeatureComponentHolder
import ru.volgadev.article_galery.api.ArticleGalleryFeatureDependencies
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.papastory.di.core.ArticleRepositoryModule
import ru.volgadev.papastory.di.core.MusicRepositoryModule

@Module(
    includes = [ArticleRepositoryModule::class, MusicRepositoryModule::class]
)
interface ArticleGalleryFeatureModule {

    companion object {
        @Provides
        fun providesArticleGalleryFeatureDependencies(
            articleRepository: ArticleRepository,
            musicRepository: MusicRepository
        ): ArticleGalleryFeatureDependencies =
            ArticleGalleryFeatureDependencies(
                articleRepository = articleRepository,
                musicRepository = musicRepository
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
