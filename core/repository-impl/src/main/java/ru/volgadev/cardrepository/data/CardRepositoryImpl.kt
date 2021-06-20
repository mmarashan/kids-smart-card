package ru.volgadev.cardrepository.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import ru.volgadev.cardrepository.data.database.CardDatabase
import ru.volgadev.cardrepository.data.datasource.CardRemoteDataSource
import ru.volgadev.cardrepository.domain.*
import ru.volgadev.cardrepository.domain.model.Card
import ru.volgadev.cardrepository.domain.model.CardCategory
import ru.volgadev.common.log.Logger
import ru.volgadev.googlebillingclientwrapper.*
import ru.volgadev.googlebillingclientwrapper.api.ItemSkuType
import ru.volgadev.googlebillingclientwrapper.api.PaymentManager
import java.io.IOException

internal class CardRepositoryImpl(
    private val remoteDataSource: CardRemoteDataSource,
    private val paymentManager: PaymentManager,
    private val database: CardDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : CardRepository {

    private val logger = Logger.get("ArticleRepositoryImpl")

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val articlesCache = HashMap<String, List<Card>>()

    override val categories: SharedFlow<List<CardCategory>> =
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
            } catch (e: IOException) {
                logger.error("Exception when load from server $e")
            }
        }
    }

    override suspend fun getCategoryCards(category: CardCategory): List<Card> =
        withContext(ioDispatcher) {

            val categoryArticles = articlesCache[category.id] ?: try {
                updateCategoryArticles(category)
            } catch (e: IOException) {
                logger.error("Exception when load from server $e")
                database.dao().getCardsByCategory(category.id)
            }
            logger.debug("${categoryArticles.size} articles")
            return@withContext categoryArticles
        }

    override suspend fun requestPaymentForCategory(category: CardCategory): Unit =
        withContext(ioDispatcher) {
            category.marketItemId?.let { paymentManager.requestPayment(skuId = it) }
        }

    override suspend fun consumePurchase(itemId: String) = withContext(ioDispatcher) {
        logger.debug("consumePurchase $itemId")
        paymentManager.consumePurchase(itemId)
    }

    override fun dispose() = scope.cancel()

    @Throws(IOException::class)
    private suspend fun updateCategories() = withContext(ioDispatcher) {
        val categories = remoteDataSource.getCategories()
        database.dao().insertAllCategories(*categories.toTypedArray())
    }

    @Throws(IOException::class)
    private suspend fun updateCategoryArticles(category: CardCategory): List<Card> =
        withContext(ioDispatcher) {
            val categoryArticles = remoteDataSource.getArticles(category)
            database.dao().insertAllArticles(*categoryArticles.toTypedArray())
            articlesCache[category.id] = categoryArticles
            return@withContext categoryArticles
        }

    @Synchronized
    @WorkerThread
    private fun updatePayedCategories(
        categories: List<CardCategory>,
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