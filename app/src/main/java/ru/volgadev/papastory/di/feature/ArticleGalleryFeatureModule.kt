package ru.volgadev.papastory.di.feature

import dagger.Binds
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
object ArticleGalleryFeatureModule {

    @Provides
    fun providesArticleGalleryFeatureDependencies(
        articleRepository: ArticleRepository,
        musicRepository: MusicRepository
    ): ArticleGalleryFeatureDependencies = ArticleGalleryFeatureDependencies(
        articleRepository = articleRepository,
        musicRepository = musicRepository
    )

    @Provides
    fun providesArticleGalleryFeatureComponentHolder() =
        ArticleGalleryFeatureComponentHolder()
}
