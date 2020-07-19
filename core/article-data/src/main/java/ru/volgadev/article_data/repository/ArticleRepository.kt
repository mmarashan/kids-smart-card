package ru.volgadev.article_data.repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import ru.volgadev.article_data.model.Article

interface ArticleRepository {

    @WorkerThread
    suspend fun updateArticles()

    fun articles(): Flow<ArrayList<Article>>

    suspend fun getArticle(id: Long): Article?
}