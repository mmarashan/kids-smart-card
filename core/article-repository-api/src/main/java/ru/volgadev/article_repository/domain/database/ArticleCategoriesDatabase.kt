package ru.volgadev.article_repository.domain.database

import ru.volgadev.article_repository.domain.model.ArticleCategory

interface ArticleCategoriesDatabase {
    fun dao(): ArticleCategoriesDao
}

interface ArticleCategoriesDao {
    fun getAll(): List<ArticleCategory>

    fun insertAll(vararg users: ArticleCategory)

    fun updateIsPaid(id: String, isPaid: Boolean)

    fun delete(user: ArticleCategory)
}