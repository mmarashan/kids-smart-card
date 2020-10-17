package ru.volgadev.cabinet_feature

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.MerchantData
import ru.volgadev.pay_lib.PaymentActivity
import ru.volgadev.pay_lib.PaymentRequest

class CabinetViewModel(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    val categories: LiveData<List<ArticleCategory>> = articleRepository.categories().asLiveData()

    // TODO: remove context from viewmodel!
    @MainThread
    fun onClickCategory(context: Context, category: String) {
        logger.debug("onClickCategory $category")
        val merchantData = MerchantData("example", "example", "example")
        val paymentRequest = PaymentRequest(category, category, 100, "USD", "RU")
        PaymentActivity.openPaymentActivity(context, merchantData, paymentRequest, true)
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        super.onCleared()
    }
}