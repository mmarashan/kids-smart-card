package ru.volgadev.cabinet_feature

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
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentType

@OptIn(InternalCoroutinesApi::class)
class CabinetViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    private val _categories = MutableLiveData<List<ArticleCategory>>()
    val categories: LiveData<List<ArticleCategory>> = _categories

    init {
        logger.debug("init")
        viewModelScope.launch(Dispatchers.Default) {
            articleRepository.categories().collect(object : FlowCollector<List<ArticleCategory>> {
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
                val paymentRequest = PaymentRequest(
                    itemId = itemId,
                    type = PaymentType.PURCHASE,
                    name = category.name,
                    description = category.description,
                    imageUrl = category.iconUrl
                )
                /**
                 * In current implementation viewModelScope disposed in onClear
                 */
                GlobalScope.launch(Dispatchers.IO) {
                    articleRepository.requestPaymentForCategory(paymentRequest)
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