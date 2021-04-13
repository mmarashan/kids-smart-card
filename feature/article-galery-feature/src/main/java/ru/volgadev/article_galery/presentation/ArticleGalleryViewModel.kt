package ru.volgadev.article_galery.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
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

    private val _category = MutableSharedFlow<ArticleCategory>()
    private val _articles = MutableSharedFlow<List<Article>>()
    private val trackChannel = Channel<MusicTrack>(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val currentCategory: SharedFlow<ArticleCategory> = _category

    val currentArticles: SharedFlow<List<Article>> = _articles

    val tracks = interactor.musicTracks()

    val trackToPlaying: Flow<MusicTrack> = trackChannel.consumeAsFlow()

    val availableCategories = interactor.availableCategories()

    @MainThread
    fun onClickCategory(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")
        viewModelScope.launch {
            _category.emit(category)
            val categoryArticles = interactor.getCategoryArticles(category)
            _articles.emit(categoryArticles)
        }
    }

    @MainThread
    fun onClickArticle(article: Article) {
        logger.debug("onClickArticle ${article.title}")
        viewModelScope.launch {
            article.onClickSounds.forEach { audioUrl ->
                val loadedAudio = interactor.getTrackFromStorage(audioUrl)
                if (loadedAudio != null) {
                    trackChannel.send(loadedAudio)
                } else {
                    trackChannel.send(
                        MusicTrack(audioUrl, filePath = null, type = MusicTrackType.ARTICLE_AUDIO)
                    )
                    interactor.loadArticleAudio(audioUrl)
                }
            }
        }
    }
}