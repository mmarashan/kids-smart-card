package ru.volgadev.cabinet_feature.presentation

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import ru.volgadev.article_repository.domain.ArticleRepository
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.cabinet_feature.BuildConfig
import ru.volgadev.common.log.Logger

@OptIn(InternalCoroutinesApi::class)
internal class CabinetViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    private val _categories = MutableLiveData<List<ArticleCategory>>()
    val categories: LiveData<List<ArticleCategory>> = _categories

    init {
        logger.debug("init")
        viewModelScope.launch(Dispatchers.Default) {
            articleRepository.categories.collect(object : FlowCollector<List<ArticleCategory>> {
                override suspend fun emit(value: List<ArticleCategory>) {
                    logger.debug("On update categories $value")
                    _categories.postValue(value)
                }
            })
        }
    }

    @MainThread
    fun onReadyToPayment(category: ArticleCategory) {
        logger.debug("onReadyToPayment ${category.name}, marketItemId = ${category.marketItemId}, isPaid = ${category.isPaid}")
        category.marketItemId?.let { itemId ->
            if (!category.isPaid) {
                logger.debug("Start payment for $itemId")
                /**
                 * In current implementation viewModelScope disposed in onClear
                 */
                GlobalScope.launch(Dispatchers.IO) {
                    articleRepository.requestPaymentForCategory(category)
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
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