package ru.volgadev.cardgallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import ru.volgadev.cardgallery.domain.ArticleGalleryInteractor
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory
import ru.volgadev.common.logger.Logger

internal class CardGalleryViewModel(
    private val interactor: ArticleGalleryInteractor
) : ViewModel() {

    private val logger = Logger.get("ArticleGalleryViewModel")

    private val categoryFlow = MutableSharedFlow<CardCategory>(replay = 1)
    private val articlesFlow = MutableSharedFlow<List<Card>>(replay = 1)

    val currentCategory: SharedFlow<CardCategory> = categoryFlow

    val currentArticles: SharedFlow<List<Card>> = articlesFlow

    val availableCategories = interactor.availableCategories()

    fun onClickCategory(category: CardCategory) = viewModelScope.launch {
        logger.debug("onClickCategory ${category.name}")
        val currentCategory = categoryFlow.replayCache.firstOrNull()
        if (category != currentCategory) {
            val categoryArticles = interactor.getCategoryArticles(category)
            categoryFlow.emit(category)
            articlesFlow.emit(categoryArticles)
        }
    }

    fun onClickArticle(card: Card) = viewModelScope.launch {
        logger.debug("onClickArticle ${card.title}")
        interactor.playCardSounds(card)
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