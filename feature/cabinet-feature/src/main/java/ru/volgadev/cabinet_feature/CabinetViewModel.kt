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

    val marketCategories: LiveData<List<MarketCategory>> = MutableLiveData<List<MarketCategory>>()

    val categories: LiveData<List<ArticleCategory>> = articleRepository.categories().asLiveData()

    val payedCategoriesItemIds: LiveData<List<String>> =
        paymentManager.ownedProductsFlow().asLiveData().map { detailsList ->
            return@map detailsList.map { skuDetails -> skuDetails.productId }
        }.distinctUntilChanged()

    @MainThread
    fun onClickCategory(category: ArticleCategory) {
        logger.debug("onClickCategory ${category.name}")

        val paymentRequest = PaymentRequest(
            itemId = "test_set",
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
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}