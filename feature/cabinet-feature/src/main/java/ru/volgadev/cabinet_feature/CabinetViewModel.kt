package ru.volgadev.cabinet_feature

import androidx.annotation.MainThread
import androidx.lifecycle.*
import ru.volgadev.article_data.model.ArticleCategory
import ru.volgadev.article_data.repository.ArticleRepository
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentResultListener
import ru.volgadev.pay_lib.PaymentType
import ru.volgadev.pay_lib.impl.DefaultPaymentActivity

class CabinetViewModel(
    private val articleRepository: ArticleRepository,
    private val paymentManager: PaymentManager
) : ViewModel() {

    private val logger = Logger.get("CabinetViewModel")

    private val categories: LiveData<List<ArticleCategory>> =
        articleRepository.categories().asLiveData()

    private val payedCategoriesItemIds: LiveData<List<String>> =
        paymentManager.ownedProductsFlow().asLiveData().map { detailsList ->
            val productIds: List<String> = detailsList.map { skuDetails -> skuDetails.productId }
            return@map productIds
        }.distinctUntilChanged()

    val marketCategories = MediatorLiveData<List<MarketCategory>>().apply {
        var _categories: ArrayList<ArticleCategory>? = null
        var _payedIds: HashSet<String>? = null

        fun checkMergeData() {
            logger.debug("checkMergeData()")
            val categories = _categories
            val payedIds = _payedIds
            logger.debug("categories = ${categories?.joinToString(",")}")
            logger.debug("payedIds = ${payedIds?.joinToString(",")}")
            if (categories != null && payedIds != null) {
                val marketCategoriesList = ArrayList<MarketCategory>()
                categories.forEach { category ->
                    val isPayed = payedIds.contains(category.marketItemId)
                    val isFree = category.marketItemId.isNullOrEmpty()
                    val marketCategory = MarketCategory(category, isFree, isPayed)
                    marketCategoriesList.add(marketCategory)
                }
                this.postValue(marketCategoriesList)
            }
        }

        addSource(categories) { catagoriesList ->
            _categories = ArrayList<ArticleCategory>().apply {
                addAll(catagoriesList)
            }
            checkMergeData()
        }
        addSource(payedCategoriesItemIds) { payedItemIds ->
            _payedIds = HashSet<String>().apply {
                addAll(payedItemIds)
            }
            checkMergeData()
        }
    }

    init {
        paymentManager.init()
    }

    @MainThread
    fun onReadyToPayment(marketCategory: MarketCategory) {
        val category = marketCategory.category
        logger.debug("onClickCategory ${category.name}")
        val itemId = category.marketItemId
        if (itemId != null && !marketCategory.isPaid) {
            logger.debug("Start payment for $itemId")
            val paymentRequest = PaymentRequest(
                itemId = itemId,
                type = PaymentType.PURCHASE,
                name = category.name,
                description = category.description,
                imageUrl = category.iconUrl
            )
            paymentManager.requestPayment(paymentRequest,
                DefaultPaymentActivity::class.java,
                object : PaymentResultListener {

                }
            )
        } else {
            if (itemId != null && BuildConfig.DEBUG){
                logger.debug("debug consume purchase $itemId")
                paymentManager.consumePurchase(itemId)
            }
        }
    }

    override fun onCleared() {
        logger.debug("onCleared()")
        paymentManager.dispose()
        super.onCleared()
    }
}