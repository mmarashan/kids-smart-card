package ru.volgadev.cabinet_feature.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.BuildConfig
import ru.volgadev.common.log.Logger

internal class CabinetViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    private val _categories = MutableLiveData<List<ArticleCategory>>()
    val categories: LiveData<List<ArticleCategory>> = _categories

    init {
        logger.debug("init")
        viewModelScope.launch {
            articleRepository.categories.collect { categories ->
                logger.debug("On update categories $categories")
                _categories.postValue(categories)
            }
        }
    }

    fun onReadyToPayment(category: ArticleCategory) {
        logger.debug("onReadyToPayment ${category.name}, marketItemId = ${category.marketItemId}, isPaid = ${category.isPaid}")
        category.marketItemId?.let { itemId ->
            if (!category.isPaid) {
                logger.debug("Start payment for $itemId")
                /**
                 * In current implementation viewModelScope disposed in onClear
                 */
                viewModelScope.launch {
                    articleRepository.requestPaymentForCategory(category)
                }
            } else {
                viewModelScope.launch {
                    if (BuildConfig.DEBUG) {
                        logger.debug("debug consume purchase $itemId")
                        articleRepository.consumePurchase(itemId)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}