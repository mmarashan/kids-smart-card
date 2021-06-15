package ru.volgadev.article_repository.data.datasource

import androidx.annotation.WorkerThread
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

@WorkerThread
internal interface ArticleRemoteDataSource {
    suspend fun getCategories(): List<ArticleCategory>
    suspend fun getArticles(category: ArticleCategory): List<Article>
}