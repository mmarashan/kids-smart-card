package ru.volgadev.cabinet_feature

import androidx.annotation.MainThread
import androidx.lifecycle.*
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentResultListener
import ru.volgadev.pay_lib.PaymentType
import ru.volgadev.pay_lib.impl.DefaultPaymentActivity

class CabinetViewModel(
    private val articleRepository: ArticleRepository,
    private val paymentManager: PaymentManager
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    val categories: LiveData<List<ArticleCategory>> = articleRepository.categories().asLiveData()

    @MainThread
    fun onReadyToPayment(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")
        val itemId = category.marketItemId
        if (itemId != null && !category.isPaid) {
            logger.debug("Start payment for $itemId")
            val paymentRequest = PaymentRequest(
                itemId = itemId,
                type = PaymentType.PURCHASE,
                name = category.name,
                description = category.description,
                imageUrl = category.iconUrl
            )
            paymentManager.requestPayment(paymentRequest,
                DefaultPaymentActivity::class.java,
                object : PaymentResultListener {

                }
            )
        } else {
            if (itemId != null && BuildConfig.DEBUG){
                logger.debug("debug consume purchase $itemId")
                paymentManager.consumePurchase(itemId)
            }
        }
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        paymentManager.dispose()
        super.onCleared()
    }
}