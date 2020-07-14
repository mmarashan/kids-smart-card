package ru.volgadev.sampledata.api

import androidx.annotation.WorkerThread
import ru.volgadev.sampledata.model.Article
import java.util.*

@WorkerThread
interface ArticleBackendApi {
    fun getUpdates(lastUpdateTime: Long): List<Article>
    fun getTags(): List<String>
}