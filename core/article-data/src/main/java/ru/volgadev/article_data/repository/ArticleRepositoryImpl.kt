package ru.volgadev.article_data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.volgadev.article_data.api.ArticleBackendApi
import ru.volgadev.article_data.model.Article
import ru.volgadev.common.log.Logger
import java.net.ConnectException

class ArticleRepositoryImpl private constructor(
    private val context: Context,
    private val articleBackendApi: ArticleBackendApi
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")
    private val articleChannel = ConflatedBroadcastChannel<ArrayList<Article>>()

    override fun articles(): Flow<ArrayList<Article>> = articleChannel.asFlow()

    override suspend fun getArticle(id: Long): Article? = withContext(Dispatchers.Default) {
        logger.debug("Get article with id $id")
        val articles = articleChannel.value
        return@withContext articles.first { article -> article.id == id }
    }

    init {
        logger.debug("Init")

        // TODO: provide scope outside
        GlobalScope.launch {
            updateArticles()
        }
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: ArticleRepositoryImpl? = null

        fun getInstance(context: Context, articleBackendApi: ArticleBackendApi) =
            instance ?: synchronized(this) {
                instance ?: ArticleRepositoryImpl(context, articleBackendApi).also { instance = it }
            }
    }

    private suspend fun updateArticles() = withContext(Dispatchers.IO) {
        try {
            val newArticles = articleBackendApi.getUpdates(System.currentTimeMillis())
            articleChannel.offer(ArrayList(newArticles))
        } catch (e: ConnectException) {
            logger.error("Exception when update article $e")
        }
    }

}