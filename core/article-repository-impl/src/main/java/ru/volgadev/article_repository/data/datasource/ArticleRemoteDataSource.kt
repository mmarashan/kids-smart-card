package ru.volgadev.article_repository.data.datasource

import androidx.annotation.WorkerThread
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

@WorkerThread
internal interface ArticleRemoteDataSource {
    fun getCategories(): List<ArticleCategory>
    fun getArticles(category: ArticleCategory): List<Article>
}