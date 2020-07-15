package ru.volgadev.article_data.api

import androidx.annotation.WorkerThread
import ru.volgadev.article_data.model.Article

@WorkerThread
interface ArticleBackendApi {
    fun getUpdates(lastUpdateTime: Long): List<Article>
    fun getTags(): List<String>
}