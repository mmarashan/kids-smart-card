package ru.volgadev.article_repository.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.SharedFlow
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

@WorkerThread
interface ArticleRepository {

    val categories: SharedFlow<List<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    suspend fun requestPaymentForCategory(category: ArticleCategory)

    suspend fun consumePurchase(itemId: String)

    fun dispose()
}