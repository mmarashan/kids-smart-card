package ru.volgadev.article_data.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.volgadev.common.DataResult
import ru.volgadev.pay_lib.PaymentRequest

@WorkerThread
interface ArticleRepository {

    fun categories(): Flow<List<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    suspend fun getArticle(id: Long): Article?

    suspend fun getArticlePages(article: Article): DataResult<List<ArticlePage>>

    suspend fun requestPaymentForCategory(paymentRequest: PaymentRequest)

    suspend fun consumePurchase(itemId: String): Boolean
}