package ru.volgadev.article_galery.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.music_data.domain.MusicRepository
import ru.volgadev.music_data.domain.model.MusicTrack

internal interface ArticleGalleryInteractor {
    fun availableCategories(): Flow<List<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    fun musicTracks(): Flow<List<MusicTrack>>

    suspend fun loadArticleAudio(url: String): MusicTrack?

    suspend fun getTrackFromStorage(url: String): MusicTrack?
}

internal class ArticleGalleryInteractorImpl(
    private val articleRepository: ArticleRepository,
    private val musicRepository: MusicRepository,
    private val isBackgroundMusicEnabled: Boolean
) : ArticleGalleryInteractor {
    override fun availableCategories(): Flow<List<ArticleCategory>> =
        articleRepository.categories.map { categories ->
            categories.filter { (it.isFree || it.isPaid) }
        }

    override suspend fun getCategoryArticles(category: ArticleCategory) =
        articleRepository.getCategoryArticles(category)

    override fun musicTracks(): Flow<List<MusicTrack>> =
        if (isBackgroundMusicEnabled) musicRepository.musicTracks() else emptyFlow()

    override suspend fun loadArticleAudio(url: String) = musicRepository.loadArticleAudio(url)

    override suspend fun getTrackFromStorage(url: String) = musicRepository.getTrackFromStorage(url)
}

