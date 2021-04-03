package ru.volgadev.article_repository.domain

import androidx.annotation.WorkerThread
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import ru.volgadev.article_repository.domain.*
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.datasource.ArticleBackendApi
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.article_repository.domain.model.ArticlePage
import ru.volgadev.common.DataResult
import ru.volgadev.common.ErrorResult
import ru.volgadev.common.SuccessResult
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.*
import ru.volgadev.pay_lib.impl.DefaultPaymentActivity
import java.net.ConnectException
import javax.inject.Inject

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class ArticleRepositoryImpl @Inject constructor(
    private val backendApi: ArticleBackendApi,
    private val paymentManager: PaymentManager,
    private val database: ArticleDatabase
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")

    private val articleChannel = MutableStateFlow<List<Article>>(value = emptyList())

    // TODO: migrate to StateFlow
    private val categoriesFlow = ConflatedBroadcastChannel<List<ArticleCategory>>()
    override fun categories(): Flow<List<ArticleCategory>> = categoriesFlow.asFlow()

    @Volatile
    private var isUpdated = false

    @Volatile
    private var productIds: List<String> = ArrayList()

    @Volatile
    private var categories = ArrayList<ArticleCategory>()

    init {
        logger.debug("init")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                loadFromServer()
            } catch (e: ConnectException) {
                logger.error("Exception when load from server $e")
                loadFromDB()
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            paymentManager.productsFlow()
                .collect(object : FlowCollector<List<MarketItem>> {
                    override suspend fun emit(items: List<MarketItem>) {
                        logger.debug("On market product list: ${items.size} categories")
                        productIds =
                            items.filter { item -> item.purchase?.purchaseState == Purchase.PurchaseState.PURCHASED }
                                .map { item -> item.skuDetails.sku }
                        updatePayedCategories(categories, productIds)
                    }
                })
        }
    }

    override suspend fun getArticle(id: Long): Article? = withContext(Dispatchers.Default) {
        logger.debug("Get article with id $id")
        updateIfNotUpdated()
        return@withContext articleChannel.value.firstOrNull { article -> article.id == id }
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
            val categories = backendApi.getCategories()
            database.dao().insertAllCategories(*categories.toTypedArray())
            return@withContext categories
        }

    @Throws(ConnectException::class)
    private suspend fun updateArticlesFromApi(categories: List<ArticleCategory>): List<Article> =
        withContext(Dispatchers.IO) {
            logger.debug("updateArticlesFromApi()")
            val articles = ArrayList<Article>()
            categories.forEach { category ->
                val categoryArticles = backendApi.getArticles(category)
                logger.debug("Load ${categoryArticles.size} articles from category ${category.name}")
                database.dao().insertAllArticles(*categoryArticles.toTypedArray())
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
        this.categories = ArrayList(categories)
        val categoriesSkuIds = categories.mapNotNull { category -> category.marketItemId }
        paymentManager.setSkuIds(categoriesSkuIds)
        updatePayedCategories(categories, productIds)
        articleChannel.value = ArrayList(articles)
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val articles = database.dao().getAllArticles()
        val dbCategories = database.dao().getAllCategories()
        logger.debug("Load ${dbCategories.size} categories and ${articles.size} articles from Db")
        articleChannel.value = ArrayList(articles)
        categories = ArrayList(dbCategories)
        val categoriesSkuIds = categories.mapNotNull { category -> category.marketItemId }
        paymentManager.setSkuIds(categoriesSkuIds)
        updatePayedCategories(dbCategories, productIds)
    }

    @WorkerThread
    override suspend fun getArticlePages(article: Article): DataResult<List<ArticlePage>> =
        withContext(Dispatchers.IO) {
            try {
                logger.debug("getArticlePages(${article.id})")
                val newArticles = backendApi.getArticlePages(article)
                return@withContext SuccessResult(newArticles)
            } catch (e: Exception) {
                return@withContext ErrorResult(e)
            }
        }

    @Synchronized
    private fun updatePayedCategories(
        categories: List<ArticleCategory>,
        payedIds: List<String>
    ) {
        val copyCategories = categories.toList()
        logger.debug("updatePayedCategories(${categories.size}, ${payedIds.size})")
        if (copyCategories.isEmpty()) return
        copyCategories.forEachIndexed { index, category ->
            val isPaid = payedIds.singleOrNull { id -> id == category.marketItemId } != null
            if (isPaid != category.isPaid) {
                category.isPaid = isPaid
                database.dao().updateCategoryIsPaid(category.id, isPaid)
            }
        }
        categoriesFlow.offer(copyCategories)
    }

    override suspend fun requestPaymentForCategory(paymentRequest: PaymentRequest) =
        withContext(Dispatchers.Default) {
            paymentManager.requestPayment(paymentRequest,
                DefaultPaymentActivity::class.java,
                object : PaymentResultListener {
                    override fun onResult(result: RequestPaymentResult) {
                        logger.debug("PaymentResultListener.onResult $result")
                    }
                }
            )
        }

    override suspend fun consumePurchase(itemId: String): Boolean =
        withContext(Dispatchers.Default) {
            logger.debug("consumePurchase $itemId")
            paymentManager.consumePurchase(itemId)
        }
}