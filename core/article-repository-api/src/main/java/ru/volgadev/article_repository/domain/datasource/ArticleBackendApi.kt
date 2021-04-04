package ru.volgadev.article_repository.domain.datasource

import androidx.annotation.WorkerThread
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

@WorkerThread
interface ArticleBackendApi {
    fun getCategories(): List<ArticleCategory>
    fun getArticles(category: ArticleCategory): List<Article>
}