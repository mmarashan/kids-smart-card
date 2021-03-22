package ru.volgadev.article_galery.api

import ru.sberdevices.module_injector.BaseDependencies
import ru.sberdevices.module_injector.ComponentHolder
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.article_galery.presentation.ArticleGalleryViewModelFactory
import ru.volgadev.music_data.domain.MusicRepository

class ArticleGalleryFeatureDependencies(
    val articleRepository: ArticleRepository,
    val musicRepository: MusicRepository
) : BaseDependencies

class ArticleGalleryFeatureComponentHolder :
    ComponentHolder<ArticleGalleryFeatureApi, ArticleGalleryFeatureDependencies> {

    private var articleRepository: ArticleRepository? = null
    private var musicRepository: MusicRepository? = null
    private var articleGalleryFeatureApiImpl: ArticleGalleryFeatureApiImpl? = null

    override fun init(dependencies: ArticleGalleryFeatureDependencies) {
        articleRepository = dependencies.articleRepository
        musicRepository = dependencies.musicRepository
    }

    @Synchronized
    override fun get(): ArticleGalleryFeatureApi {
        val articleRepository = articleRepository
        val musicRepository = musicRepository
        checkNotNull(articleRepository) { "articleRepository was not initialized!" }
        checkNotNull(musicRepository) { "musicRepository was not initialized!" }
        if (articleGalleryFeatureApiImpl == null) {
            ArticleGalleryViewModelFactory.inject(articleRepository, musicRepository)
            articleGalleryFeatureApiImpl = ArticleGalleryFeatureApiImpl()
        }
        return articleGalleryFeatureApiImpl!!
    }

    override fun clear() {
        articleRepository = null
        musicRepository = null
        articleGalleryFeatureApiImpl = null
        ArticleGalleryViewModelFactory.clear()
    }
}