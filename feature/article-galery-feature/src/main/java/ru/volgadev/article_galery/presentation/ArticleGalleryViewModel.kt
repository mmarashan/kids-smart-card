package ru.volgadev.article_galery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import ru.volgadev.article_galery.domain.ArticleGalleryInteractor
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.log.Logger
import ru.volgadev.music_data.domain.model.MusicTrack
import ru.volgadev.music_data.domain.model.MusicTrackType

internal class ArticleGalleryViewModel(
    private val interactor: ArticleGalleryInteractor
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val categoryFlow = MutableSharedFlow<ArticleCategory>(replay = 1)
    private val articlesFlow = MutableSharedFlow<List<Article>>(replay = 1)
    private val trackFlow = MutableSharedFlow<MusicTrack>(replay = 0)

    val currentCategory: SharedFlow<ArticleCategory> = categoryFlow

    val currentArticles: SharedFlow<List<Article>> = articlesFlow

    val tracks = interactor.musicTracks()

    val trackToPlaying: SharedFlow<MusicTrack> = trackFlow

    val availableCategories = interactor.availableCategories()

    fun onClickCategory(category: ArticleCategory) = viewModelScope.launch {
        logger.debug("onClickCategory ${category.name}")
        val categoryArticles = interactor.getCategoryArticles(category)
        categoryFlow.emit(category)
        articlesFlow.emit(categoryArticles)
    }

    fun onClickArticle(article: Article) = viewModelScope.launch {
        logger.debug("onClickArticle ${article.title}")
        article.onClickSounds.forEach { audioUrl ->
            val loadedAudio = interactor.getTrackFromStorage(audioUrl)
            if (loadedAudio != null) {
                trackFlow.emit(loadedAudio)
            } else {
                trackFlow.emit(
                    MusicTrack(audioUrl, filePath = null, type = MusicTrackType.ARTICLE_AUDIO)
                )
                interactor.loadArticleAudio(audioUrl)
            }
        }
    }
}