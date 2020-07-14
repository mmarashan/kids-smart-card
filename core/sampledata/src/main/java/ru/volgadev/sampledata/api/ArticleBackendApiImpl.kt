package ru.volgadev.sampledata.api

import androidx.annotation.WorkerThread
import ru.volgadev.sampledata.model.Article

@WorkerThread
class ArticleBackendApiImpl: ArticleBackendApi{

    override fun getUpdates(lastUpdateTime: Long): List<Article> {
        TODO("Not yet implemented")
    }

    override fun getTags(): List<String> {
        TODO("Not yet implemented")
    }

}