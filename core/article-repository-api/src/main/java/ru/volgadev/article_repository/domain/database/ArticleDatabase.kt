package ru.volgadev.article_repository.domain.database

import ru.volgadev.article_repository.domain.model.Article

interface ArticleDatabase {
    fun dao(): ArticleDao
}

interface ArticleDao {
    fun getAll(): List<Article>

    fun loadAllByIds(articleIds: IntArray): List<Article>

    fun insertAll(vararg articles: Article)

    fun delete(article: Article)

    fun isRowIsExist(id: Int): Boolean
}