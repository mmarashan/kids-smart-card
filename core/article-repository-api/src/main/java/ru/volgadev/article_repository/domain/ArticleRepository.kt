package ru.volgadev.article_repository.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.SharedFlow
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.pay_lib.PaymentRequest

@WorkerThread
interface ArticleRepository {

    val categories: SharedFlow<List<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    suspend fun requestPaymentForCategory(paymentRequest: PaymentRequest)

    suspend fun consumePurchase(itemId: String): Boolean

    fun dispose()
}