package ru.volgadev.article_data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
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
import ru.volgadev.pay_lib.*
import ru.volgadev.pay_lib.impl.DefaultPaymentActivity
import java.net.ConnectException

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class ArticleRepositoryImpl(
    private val context: Context,
    private val articleBackendApi: ArticleBackendApi,
    private val paymentManager: PaymentManager
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")

    private val articleChannel = MutableStateFlow<ArrayList<Article>>(value = ArrayList())
    private val categoriesFlow = ConflatedBroadcastChannel<List<ArticleCategory>>()

    private val articlesDb: ArticleDatabase by lazy {
        ArticleDatabase.getInstance(context)
    }

    private val categoriesDb: ArticleCategoriesDatabase by lazy {
        ArticleCategoriesDatabase.getInstance(context)
    }

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
            logger.debug("loadData..")
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
        this.categories = ArrayList(categories)
        val categoriesSkuIds = categories.mapNotNull { category -> category.marketItemId }
        paymentManager.setSkuIds(categoriesSkuIds)
        updatePayedCategories(categories, productIds)
        articleChannel.value = ArrayList(articles)
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val articles = articlesDb.dao().getAll()
        val dbCategories = categoriesDb.dao().getAll()
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
                val newArticles = articleBackendApi.getArticlePages(article)
                logger.debug("${newArticles.size} pages")
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
                categoriesDb.dao().updateIsPaid(category.id, isPaid)
            }
        }
        logger.debug("categories = ${categories.joinToString(",")}")
        logger.debug("payedIds = ${payedIds.joinToString(",")}")
        categoriesFlow.offer(copyCategories)
    }

    override suspend fun requestPaymentForCategory(paymentRequest: PaymentRequest) =
        withContext(Dispatchers.Default) {
            logger.debug("requestPaymentForCategory($paymentRequest)")
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