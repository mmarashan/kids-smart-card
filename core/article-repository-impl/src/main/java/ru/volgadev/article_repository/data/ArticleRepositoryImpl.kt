package ru.volgadev.article_repository.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.volgadev.article_repository.data.database.ArticleDatabase
import ru.volgadev.article_repository.data.datasource.ArticleRemoteDataSource
import ru.volgadev.article_repository.domain.*
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.*
import ru.volgadev.pay_lib.impl.DefaultPaymentActivity
import java.net.ConnectException
import javax.inject.Inject

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class ArticleRepositoryImpl @Inject constructor(
    private val remoteDataSource: ArticleRemoteDataSource,
    private val paymentManager: PaymentManager,
    private val database: ArticleDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")

    private val scope = CoroutineScope(SupervisorJob())

    private val articlesCache = HashMap<String, List<Article>>()

    private val categoriesStateFlow = MutableStateFlow<List<ArticleCategory>>(emptyList())
    override fun categories(): StateFlow<List<ArticleCategory>> = categoriesStateFlow

    @Volatile
    private var productIds: List<String> = ArrayList()

    init {
        scope.launch {
            val categories = try {
                updateCategories()
            } catch (e: ConnectException) {
                logger.error("Exception when load from server $e")
                loadCategoriesFromDB()
            }

            val categoriesSkuIds = categories.mapNotNull { category -> category.marketItemId }
            paymentManager.setSkuIds(categoriesSkuIds)
            updatePayedCategories(categories, productIds)
        }

        scope.launch {
            paymentManager.productsFlow().collect(object : FlowCollector<List<MarketItem>> {
                override suspend fun emit(items: List<MarketItem>) {
                    logger.debug("On market product list: ${items.size} categories")
                    productIds = items.filter { it.isPurchased() }.map { it.skuDetails.sku }
                    val categories = categoriesStateFlow.value
                    updatePayedCategories(categories, productIds)
                }
            })
        }
    }

    override suspend fun getCategoryArticles(category: ArticleCategory): List<Article> =
        withContext(Dispatchers.Default) {
            val categoryArticles = articlesCache[category.id] ?: updateCategoryArticles(category)
            logger.debug("${categoryArticles.size} articles")
            return@withContext categoryArticles
        }

    override suspend fun requestPaymentForCategory(paymentRequest: PaymentRequest) =
        withContext(ioDispatcher) {
            paymentManager.requestPayment(paymentRequest,
                DefaultPaymentActivity::class.java,
                object : PaymentResultListener {
                    override fun onResult(result: RequestPaymentResult) {
                        logger.debug("PaymentResultListener.onResult $result")
                    }
                }
            )
        }

    override suspend fun consumePurchase(itemId: String): Boolean = withContext(ioDispatcher) {
        logger.debug("consumePurchase $itemId")
        paymentManager.consumePurchase(itemId)
    }

    override fun dispose() = scope.cancel()

    @Throws(ConnectException::class)
    private suspend fun updateCategories(): List<ArticleCategory> = withContext(Dispatchers.IO) {
        val categories = remoteDataSource.getCategories()
        database.dao().insertAllCategories(*categories.toTypedArray())
        return@withContext categories
    }

    @Throws(ConnectException::class)
    private suspend fun updateCategoryArticles(category: ArticleCategory): List<Article> =
        withContext(Dispatchers.IO) {
            val categoryArticles = remoteDataSource.getArticles(category)
            database.dao().insertAllArticles(*categoryArticles.toTypedArray())
            articlesCache[category.id] = categoryArticles
            return@withContext categoryArticles
        }

    private suspend fun loadCategoriesFromDB(): List<ArticleCategory> = withContext(ioDispatcher) {
        val articles = database.dao().getAllArticles()
        val categories = database.dao().getAllCategories()
        logger.debug("Load ${categories.size} categories and ${articles.size} articles from Db")
        categories.forEach { category ->
            val categoryArticles = articles.filter { it.categoryId == category.id }
            articlesCache[category.id] = categoryArticles
        }
        return@withContext categories
    }

    @Synchronized
    @WorkerThread
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
        categoriesStateFlow.tryEmit(copyCategories)
    }
}