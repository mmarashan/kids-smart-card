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
import ru.volgadev.article_data.model.Article
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.model.ArticlePage
import ru.volgadev.article_data.storage.ArticleCategoriesDatabase
import ru.volgadev.article_data.storage.ArticleDatabase
import ru.volgadev.common.DataResult
import ru.volgadev.common.ErrorResult
import ru.volgadev.common.SuccessResult
import ru.volgadev.common.log.Logger
import java.net.ConnectException

class ArticleRepositoryImpl(
    private val context: Context,
    private val articleBackendApi: ArticleBackendApi
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")
    private val articleChannel = ConflatedBroadcastChannel<ArrayList<Article>>()
    private val categoriesChannel = ConflatedBroadcastChannel<ArrayList<ArticleCategory>>()

    private val articlesDb: ArticleDatabase by lazy {
        ArticleDatabase.getInstance(context)
    }

    private val categoriesDb: ArticleCategoriesDatabase by lazy {
        ArticleCategoriesDatabase.getInstance(context)
    }

    override fun categories(): Flow<ArrayList<ArticleCategory>> = categoriesChannel.asFlow()

    @Volatile
    private var isUpdated = false

    init {
        logger.debug("init")
        CoroutineScope(Dispatchers.Default).launch {
            logger.debug("loadData..")
            try {
                loadFromServer()
            } catch (e: ConnectException) {
                logger.error("Exception when load from server $e")
                loadFromDB()
            }
        }
    }

    override suspend fun getArticle(id: Long): Article? = withContext(Dispatchers.Default) {
        logger.debug("Get article with id $id")
        updateIfNotUpdated()
        return@withContext articleChannel.value.first { article -> article.id == id }
    }

    override suspend fun getCategoryArticles(category: ArticleCategory): List<Article> =
        withContext(Dispatchers.Default) {
            logger.debug("getCategoryArticles(${category.name})")
            logger.debug("Articles(${articleChannel.value.joinToString(",")})")
            updateIfNotUpdated()
            val categoryArticles =
                articleChannel.value.filter { article -> article.categoryId == category.id }
            logger.debug("getCategoryArticles(${category.name}) - ${categoryArticles.size} articles")
            return@withContext categoryArticles
        }

    @Throws(ConnectException::class)
    private suspend fun updateCategoriesFromApi(): List<ArticleCategory> =
        withContext(Dispatchers.IO) {
            val categories = articleBackendApi.getCategories()
            categoriesDb.dao().insertAll(*categories.toTypedArray())
            return@withContext categories
        }

    @Throws(ConnectException::class)
    private suspend fun updateArticlesFromApi(categories: List<ArticleCategory>): List<Article> =
        withContext(Dispatchers.IO) {
            logger.debug("updateArticlesFromApi()")
            val articles = ArrayList<Article>()
            categories.forEach { category ->
                val categoryArticles = articleBackendApi.getArticles(category)
                logger.debug("Load ${categoryArticles.size} articles from category ${category.name}")
                articlesDb.dao().insertAll(*categoryArticles.toTypedArray())
                articles.addAll(categoryArticles)
            }
            return@withContext articles
        }

    private suspend fun updateIfNotUpdated() {
        if (isUpdated) return
        try {
            loadFromServer()
        } catch (e: ConnectException) {
            logger.error("Exception when load from server $e")
        }
    }

    @Throws(ConnectException::class)
    private suspend fun loadFromServer() {
        logger.debug("loadFromServer()")
        val categories = updateCategoriesFromApi()
        val articles = updateArticlesFromApi(categories)
        isUpdated = true
        logger.debug("loadFromServer() OK")
        categoriesChannel.offer(ArrayList(categories))
        articleChannel.offer(ArrayList(articles))
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val articles = articlesDb.dao().getAll()
        val categories = categoriesDb.dao().getAll()
        logger.debug("Load ${categories.size} categories and ${articles.size} articles from Db")
        articleChannel.offer(ArrayList(articles))
        categoriesChannel.offer(ArrayList(categories))
    }

    @WorkerThread
    override suspend fun getArticlePages(article: Article): DataResult<List<ArticlePage>> =
        withContext(Dispatchers.IO) {
            try {
                logger.debug("getArticlePages(${article.id})")
                val newArticles = articleBackendApi.getArticlePages(article)
                logger.debug("${newArticles.size} pages")
                return@withContext SuccessResult(newArticles)
            } catch (e: Exception) {
                return@withContext ErrorResult(e)
            }
        }
}