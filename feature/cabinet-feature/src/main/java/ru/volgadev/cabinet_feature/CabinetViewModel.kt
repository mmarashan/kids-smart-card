package ru.volgadev.cabinet_feature

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.MerchantData
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest

class CabinetViewModel(
    private val articleRepository: ArticleRepository,
    private val paymentManager: PaymentManager
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    val categories: LiveData<List<ArticleCategory>> = articleRepository.categories().asLiveData()

    @MainThread
    fun onClickCategory(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")

        val merchantData = MerchantData("example", "example", "example")
        val paymentRequest = PaymentRequest(
            category.name, category.description, 100,
            "USD", "RU"
        )
        paymentManager.requestPayment(merchantData, paymentRequest, true)
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}