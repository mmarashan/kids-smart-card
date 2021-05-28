package ru.volgadev.article_repository.data.database

import kotlinx.coroutines.flow.Flow
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory

internal interface ArticleDatabase {
    fun dao(): ArticleDao
}

internal interface ArticleDao {

    fun articles(): List<Article>

    fun getArticlesByCategory(categoryId: String): List<Article>

    fun insertAllArticles(vararg articles: Article)

    fun delete(article: Article)

    fun isArticleExists(id: Int): Boolean

    fun categories(): Flow<List<ArticleCategory>>

    fun insertAllCategories(vararg users: ArticleCategory)

    fun updateCategoryIsPaid(id: String, isPaid: Boolean)

    fun delete(user: ArticleCategory)
}