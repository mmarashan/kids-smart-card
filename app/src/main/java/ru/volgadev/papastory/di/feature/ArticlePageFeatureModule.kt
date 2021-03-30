package ru.volgadev.papastory.di.feature

import dagger.Module
import dagger.Provides
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_page.api.ArticlePageFeatureComponentHolder
import ru.volgadev.article_page.api.ArticlePageFeatureDependencies
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.papastory.di.core.ArticleRepositoryModule
import ru.volgadev.papastory.di.core.MusicRepositoryModule

@Module(
    includes = [ArticleRepositoryModule::class, MusicRepositoryModule::class]
)
object ArticlePageFeatureModule {

    @Provides
    fun providesArticlePageFeatureDependencies(
        articleRepository: ArticleRepository,
        musicRepository: MusicRepository
    ): ArticlePageFeatureDependencies = ArticlePageFeatureDependencies(
        articleRepository = articleRepository,
        musicRepository = musicRepository
    )

    @Provides
    fun providesArticlePageFeatureComponentHolder() =
        ArticlePageFeatureComponentHolder()
}
