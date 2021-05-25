package ru.volgadev.article_repository.domain

import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import ru.volgadev.article_repository.domain.*
import ru.volgadev.article_repository.domain.database.ArticleDatabase
import ru.volgadev.article_repository.domain.datasource.ArticleBackendApi
import ru.volgadev.article_repository.domain.model.Article
import ru.volgadev.article_repository.domain.model.ArticleCategory
import ru.volgadev.common.log.Logger
import ru.volgadev.googlebillingclientwrapper.*
import ru.volgadev.googlebillingclientwrapper.api.ItemSkuType
import ru.volgadev.googlebillingclientwrapper.api.PaymentManager
import java.net.ConnectException
import javax.inject.Inject

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class ArticleRepositoryImpl @Inject constructor(
    private val backendApi: ArticleBackendApi,
    private val paymentManager: PaymentManager,
    private val database: ArticleDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : ArticleRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")

    private val scope = CoroutineScope(SupervisorJob())

    private val articlesCache = HashMap<String, List<Article>>()

    override val categories: SharedFlow<List<ArticleCategory>> =
        database.dao().categories().distinctUntilChanged().shareIn(
            scope = scope,
            started = Eagerly,
            replay = 1
        )

    @Volatile
    private var productIds: List<String> = ArrayList()

    init {
        scope.launch {
            categories.collect { categories ->
                val categoriesSkuIds = categories.mapNotNull { it.marketItemId }
                paymentManager.setProjectSkuIds(categoriesSkuIds, ItemSkuType.IN_APP)
                updatePayedCategories(categories, productIds)
            }
        }

        scope.launch {
            paymentManager.ownedProducts.collect { items ->
                logger.debug("On market product list: ${items.size} categories")
                productIds = items.filter { it.isPurchased() }.map { it.skuDetails.sku }
                val categories = database.dao().categories().first()
                updatePayedCategories(categories, productIds)
            }
        }

        scope.launch {
            try {
                updateCategories()
            } catch (e: ConnectException) {
                logger.error("Exception when load from server $e")
            }
        }
    }

    override suspend fun getCategoryArticles(category: ArticleCategory): List<Article> =
        withContext(ioDispatcher) {
            val categoryArticles = articlesCache[category.id] ?: try {
                updateCategoryArticles(category)
            } catch (e: ConnectException) {
                database.dao().getArticlesByCategory(category.id)
            }
            logger.debug("${categoryArticles.size} articles")
            return@withContext categoryArticles
        }

    override suspend fun requestPaymentForCategory(category: ArticleCategory): Unit =
        withContext(ioDispatcher) {
            category.marketItemId?.let { paymentManager.requestPayment(skuId = it) }
        }

    override suspend fun consumePurchase(itemId: String): Boolean = withContext(ioDispatcher) {
        logger.debug("consumePurchase $itemId")
        paymentManager.consumePurchase(itemId)
    }

    override fun dispose() = scope.cancel()

    @Throws(ConnectException::class)
    private suspend fun updateCategories() = withContext(ioDispatcher) {
        val categories = backendApi.getCategories()
        database.dao().insertAllCategories(*categories.toTypedArray())
    }

    @Throws(ConnectException::class)
    private suspend fun updateCategoryArticles(category: ArticleCategory): List<Article> =
        withContext(Dispatchers.IO) {
            val categoryArticles = backendApi.getArticles(category)
            database.dao().insertAllArticles(*categoryArticles.toTypedArray())
            articlesCache[category.id] = categoryArticles
            return@withContext categoryArticles
        }

    @Synchronized
    @WorkerThread
    private fun updatePayedCategories(
        categories: List<ArticleCategory>,
        payedIds: List<String>
    ) {
        logger.debug("updatePayedCategories(${categories.size}, ${payedIds.size})")
        categories.forEachIndexed { index, category ->
            val isPaid = payedIds.singleOrNull { id -> id == category.marketItemId } != null
            if (isPaid != category.isPaid) {
                category.isPaid = isPaid
                database.dao().updateCategoryIsPaid(category.id, isPaid)
            }
        }
    }
}