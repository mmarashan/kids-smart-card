package ru.volgadev.article_data.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticlePage

@WorkerThread
interface ArticleRepository {

    suspend fun updateArticles()

    fun articles(): Flow<ArrayList<Article>>

    suspend fun getArticle(id: Long): Article?

    suspend fun getArticlePages(article: Article): List<ArticlePage>
}