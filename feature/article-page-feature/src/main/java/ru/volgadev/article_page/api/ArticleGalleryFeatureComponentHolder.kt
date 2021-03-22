package ru.volgadev.article_page.api

import ru.sberdevices.module_injector.BaseDependencies
import ru.sberdevices.module_injector.ComponentHolder
import ru.volgadev.article_data.domain.ArticleRepository
import ru.volgadev.article_page.presentation.ArticlePageViewModelFactory
import ru.volgadev.music_data.domain.MusicRepository

class ArticlePageFeatureDependencies(
    val articleRepository: ArticleRepository,
    val musicRepository: MusicRepository
) : BaseDependencies

class ArticlePageFeatureComponentHolder :
    ComponentHolder<ArticlePageFeatureApi, ArticlePageFeatureDependencies> {

    private var articleRepository: ArticleRepository? = null
    private var musicRepository: MusicRepository? = null
    private var articlePageFeatureApiImpl: ArticlePageFeatureApiImpl? = null

    override fun init(dependencies: ArticlePageFeatureDependencies) {
        articleRepository = dependencies.articleRepository
        musicRepository = dependencies.musicRepository
    }

    @Synchronized
    override fun get(): ArticlePageFeatureApi {
        val articleRepository = articleRepository
        val musicRepository = musicRepository
        checkNotNull(articleRepository) { "articleRepository was not initialized!" }
        checkNotNull(musicRepository) { "musicRepository was not initialized!" }
        if (articlePageFeatureApiImpl == null) {
            ArticlePageViewModelFactory.inject(articleRepository, musicRepository)
            articlePageFeatureApiImpl = ArticlePageFeatureApiImpl()
        }
        return articlePageFeatureApiImpl!!
    }

    override fun clear() {
        articleRepository = null
        musicRepository = null
        ArticlePageViewModelFactory.clear()
    }
}