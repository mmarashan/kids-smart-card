package ru.volgadev.cabinet_feature

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentType
import ru.volgadev.pay_lib.impl.DefaultPaymentActivity

class CabinetViewModel(
    private val articleRepository: ArticleRepository,
    private val paymentManager: PaymentManager
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    val categories: LiveData<List<ArticleCategory>> = articleRepository.categories().asLiveData()

    @MainThread
    fun onClickCategory(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")

        val paymentRequest = PaymentRequest(
            "test_set", PaymentType.PURCHASE
        )
        paymentManager.requestPayment(paymentRequest, DefaultPaymentActivity::class.java)
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}