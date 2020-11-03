package ru.volgadev.article_data.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.model.ArticlePage
import ru.volgadev.common.DataResult

@WorkerThread
interface ArticleRepository {

    fun categories(): Flow<ArrayList<ArticleCategory>>

    suspend fun getCategoryArticles(category: ArticleCategory): List<Article>

    suspend fun getArticle(id: Long): Article?

    suspend fun getArticlePages(article: Article): DataResult<List<ArticlePage>>
}