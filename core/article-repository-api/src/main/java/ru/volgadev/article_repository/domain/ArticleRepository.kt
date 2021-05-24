package ru.volgadev.article_repository.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.StateFlow
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

@WorkerThread
interface ArticleRepository {

    fun categories(): StateFlow<List<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    suspend fun requestPaymentForCategory(category: ArticleCategory)

    suspend fun consumePurchase(itemId: String): Boolean

    fun dispose()
}