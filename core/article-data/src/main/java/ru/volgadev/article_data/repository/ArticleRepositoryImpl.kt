package ru.volgadev.article_data.repository

import android.content.Context
import androidx.annotation.WorkerThread
import com.anjlab.android.iab.v3.SkuDetails
import kotlinx.coroutines.*
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
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentResultListener
import ru.volgadev.pay_lib.RequestPaymentResult
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
    private val categoriesFlow = MutableStateFlow<ArrayList<ArticleCategory>>(value = ArrayList())

    private val articlesDb: ArticleDatabase by lazy {
        ArticleDatabase.getInstance(context)
    }

    private val categoriesDb: ArticleCategoriesDatabase by lazy {
        ArticleCategoriesDatabase.getInstance(context)
    }

    override fun categories(): Flow<ArrayList<ArticleCategory>> =
        categoriesFlow.filter { list -> list.isNotEmpty() }

    @Volatile
    private var isUpdated = false

    @Volatile
    private var ownedProductIds: List<String> = ArrayList()
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
            paymentManager.ownedProductsFlow()
                .collect(object : FlowCollector<ArrayList<SkuDetails>> {
                    override suspend fun emit(value: ArrayList<SkuDetails>) {
                        logger.debug("New owned product list: ${value.size} categories")
                        ownedProductIds = value.map { skuDetails -> skuDetails.productId }
                        updatePayedCategories(categories, ownedProductIds)
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
        updatePayedCategories(this.categories, ownedProductIds)
        articleChannel.value = ArrayList(articles)
    }

    private suspend fun loadFromDB() = withContext(Dispatchers.Default) {
        val articles = articlesDb.dao().getAll()
        val dbCategories = categoriesDb.dao().getAll()
        logger.debug("Load ${categories.size} categories and ${articles.size} articles from Db")
        articleChannel.value = ArrayList(articles)
        categories = ArrayList(dbCategories)
        updatePayedCategories(categories, ownedProductIds)
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

    private fun updatePayedCategories(
        categories: ArrayList<ArticleCategory>,
        payedIds: List<String>
    ) {
        logger.debug("updatePayedCategories(${categories.size}, ${payedIds.size})")
        if (categories.isEmpty()) return
        logger.debug("categories = ${categories.joinToString(",")}")
        logger.debug("payedIds = ${payedIds.joinToString(",")}")
        categories.forEach { category ->
            val isPayed = payedIds.contains(category.marketItemId)
            category.isPaid = isPayed
        }
        categoriesFlow.value = ArrayList(categories)
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