package ru.volgadev.article_data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.volgadev.article_data.api.ArticleBackendApi
import ru.volgadev.article_data.storage.ArticleDatabase
import ru.volgadev.article_data.model.Article
import ru.volgadev.common.log.Logger
import java.net.ConnectException

class ArticleRepositoryImpl private constructor(
    private val context: Context,
    private val articleBackendApi: ArticleBackendApi
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")
    private val articleChannel = ConflatedBroadcastChannel<ArrayList<Article>>()

    private val articlesDb: ArticleDatabase by lazy {
        ArticleDatabase.getInstance(context)
    }

    override fun articles(): Flow<ArrayList<Article>> = articleChannel.asFlow()

    override suspend fun getArticle(id: Long): Article? = withContext(Dispatchers.Default) {
        logger.debug("Get article with id $id")
        val articles = articleChannel.value
        return@withContext articles.first { article -> article.id == id }
    }

    init {
        logger.debug("Init")
        CoroutineScope(Dispatchers.Default).launch {
            updateArticles()
        }
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: ArticleRepositoryImpl? = null

        fun getInstance(
            context: Context, articleBackendApi: ArticleBackendApi
        ) =
            instance ?: synchronized(this) {
                instance ?: ArticleRepositoryImpl(
                    context,
                    articleBackendApi
                ).also { instance = it }
            }
    }

    @WorkerThread
    override suspend fun updateArticles() {
        try {
            logger.debug("Try to update data")
            updateArticlesFromApi()
        } catch (e: ConnectException) {
            logger.error("Exception when update article $e")
            logger.debug("Load data from DB")
            loadFromDB()
        }
    }

    @Throws(ConnectException::class)
    private suspend fun updateArticlesFromApi() = withContext(Dispatchers.IO) {
        val newArticles = articleBackendApi.getUpdates(System.currentTimeMillis())
        articlesDb.userDao().insertAll(*newArticles.toTypedArray())
        articleChannel.offer(ArrayList(newArticles))
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val articles = articlesDb.userDao().getAll()
        logger.debug("Load ${articles.size} entities from Db")
        articleChannel.offer(ArrayList(articles))
    }

}