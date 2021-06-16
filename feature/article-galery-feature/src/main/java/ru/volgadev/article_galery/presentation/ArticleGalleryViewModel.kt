package ru.volgadev.article_galery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.volgadev.article_galery.domain.ArticleGalleryInteractor
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.log.Logger

internal class ArticleGalleryViewModel(
    private val interactor: ArticleGalleryInteractor
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val categoryFlow = MutableSharedFlow<ArticleCategory>(replay = 1)
    private val articlesFlow = MutableSharedFlow<List<Article>>(replay = 1)

    val currentCategory: SharedFlow<ArticleCategory> = categoryFlow

    val currentArticles: SharedFlow<List<Article>> = articlesFlow

    val availableCategories = interactor.availableCategories()

    fun onClickCategory(category: ArticleCategory) = viewModelScope.launch {
        logger.debug("onClickCategory ${category.name}")
        val currentCategory = categoryFlow.replayCache.firstOrNull()
        if (category != currentCategory) {
            val categoryArticles = interactor.getCategoryArticles(category)
            categoryFlow.emit(category)
            articlesFlow.emit(categoryArticles)
        }
    }

    fun onClickArticle(article: Article) = viewModelScope.launch {
        logger.debug("onClickArticle ${article.title}")
        interactor.playCardSounds(article)
    }

    fun onToggleMusicPlayer(isChecked: Boolean) = viewModelScope.launch {
        if (isChecked) interactor.startBackgroundPlayer()
        else interactor.pauseBackgroundPlayer()
    }

    fun onClickNextTrack() = viewModelScope.launch {
        interactor.nextTrack()
    }

    fun onClickPreviousTrack() = viewModelScope.launch {
        interactor.previousTrack()
    }
}