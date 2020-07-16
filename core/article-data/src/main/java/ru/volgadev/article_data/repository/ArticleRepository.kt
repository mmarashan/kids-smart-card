package ru.volgadev.article_data.repository

import kotlinx.coroutines.flow.Flow
import ru.volgadev.article_data.model.Article

interface ArticleRepository {
    fun articles(): Flow<ArrayList<Article>>

    suspend fun getArticle(id: Long): Article?
}