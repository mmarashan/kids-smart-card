package ru.volgadev.cabinet_feature

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentType

class CabinetViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    val categories: LiveData<List<ArticleCategory>> = articleRepository.categories().asLiveData()

    @MainThread
    fun onReadyToPayment(category: ArticleCategory) {
        logger.debug("onReadyToPayment ${category.name}")
        val itemId = category.marketItemId
        if (itemId != null /*&& !category.isPaid*/) {
            logger.debug("Start payment for $itemId")
            val paymentRequest = PaymentRequest(
                itemId = itemId,
                type = PaymentType.PURCHASE,
                name = category.name,
                description = category.description,
                imageUrl = category.iconUrl
            )
            viewModelScope.launch {
                articleRepository.requestPaymentForCategory(paymentRequest)
            }
        }
//        else {
//            viewModelScope.launch {
//                if (itemId != null && BuildConfig.DEBUG) {
//                    logger.debug("debug consume purchase $itemId")
//                    articleRepository.consumePurchase(itemId)
//                }
//            }
//        }
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}