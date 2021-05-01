package ru.volgadev.article_repository.data.database

import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

interface ArticleDatabase {
    fun dao(): ArticleDao
}

interface ArticleDao {
    fun getAllArticles(): List<Article>

    fun loadAllByIds(articleIds: IntArray): List<Article>

    fun insertAllArticles(vararg articles: Article)

    fun delete(article: Article)

    fun isArticleExists(id: Int): Boolean

    fun getAllCategories(): List<ArticleCategory>

    fun insertAllCategories(vararg users: ArticleCategory)

    fun updateCategoryIsPaid(id: String, isPaid: Boolean)

    fun delete(user: ArticleCategory)
}