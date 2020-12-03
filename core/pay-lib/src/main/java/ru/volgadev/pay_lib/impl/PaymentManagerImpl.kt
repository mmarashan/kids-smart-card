package ru.volgadev.pay_lib.impl

import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.*


@ExperimentalCoroutinesApi
internal class PaymentManagerImpl(
    private val context: Context,
    private val googlePlayLicenseKey: String
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val ownedProductsStateFlow = MutableStateFlow<List<MarketItem>>(listOf())
    private val ownedSubscriptionStateFlow = MutableStateFlow<List<MarketItem>>(listOf())

    private val skuDetailsMap = HashMap<String, SkuDetails>()

    private val billingClient =
        BillingClient.newBuilder(context).setListener(object : PurchasesUpdatedListener {

            /**
             * Listener interface for purchase updates which happen when,
             * for example, the user buys something within the app or by initiating a purchase
             * from Google Play Store.
             */
            override fun onPurchasesUpdated(
                billingResult: BillingResult,
                purchases: MutableList<Purchase>?
            ) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    //здесь мы можем запросить информацию о товарах и покупках
                }
            }
        }).enablePendingPurchases().build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {

            /**
             * Callback for setup process. This listener's onBillingSetupFinished(BillingResult)
             * method is called when the setup process is complete.
             */
            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    //здесь мы можем запросить информацию о товарах и покупках
                    val idsList = listOf("numbers_pro_ru", "test_item_2")
                    querySkuDetails(idsList)

                    val purchasesList = queryPurchases() //запрос о покупках

                    //если товар уже куплен, предоставить его пользователю
                    for (i in purchasesList.indices) {
                        val purchaseId = purchasesList[i].sku

                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                //сюда мы попадем если что-то пойдет не так
            }
        })
    }

    private fun querySkuDetails(skuIds: List<String>) {
        val param = SkuDetailsParams.newBuilder().setSkusList(skuIds)
            .setType(BillingClient.SkuType.INAPP).build()
        scope.launch {
            billingClient.querySkuDetailsAsync(
                param
            ) { billingResult, skuDetails ->
                if (billingResult.responseCode == 0 && skuDetails != null) {
                    skuDetails.forEach { skuDetails ->
                        skuDetailsMap.put(skuDetails.sku, skuDetails)
                    }
                    ownedProductsStateFlow.value = skuDetails.toList().map { d -> d.toMarketItem() }
                }
            }
        }
    }

    private fun queryPurchases(): List<Purchase> {
        val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        return purchasesResult.purchasesList ?: listOf()
    }

    private var resultListener: PaymentResultListener? = null

    override fun isAvailable(): Boolean {
        logger.debug("isAvailable()")
        val isAvailable = false
        logger.debug("BillingProcessor.isAvailable = $isAvailable")
        return isAvailable
    }

    override fun requestPayment(
        paymentRequest: PaymentRequest,
        // TODO: maybe current activity instance
        activityClass: Class<out BillingProcessorActivity>,
        resultListener: PaymentResultListener
    ) {
        this.resultListener = resultListener
        val item = skuDetailsMap.get(paymentRequest.itemId)
        if (item == null) {
            logger.error("Call paymentRequest() for not exist item")
            resultListener.onResult(RequestPaymentResult.PAYMENT_ERROR)
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(item)
            .build()
        val responseCode =
            billingClient.launchBillingFlow(activityClass.newInstance(), billingFlowParams)
    }

    override fun consumePurchase(itemId: String): Boolean {
        logger.debug("consumePurchase($itemId)")
        return false
    }

    override fun ownedProductsFlow(): StateFlow<List<MarketItem>> = ownedProductsStateFlow

    override fun ownedSubscriptionsFlow(): StateFlow<List<MarketItem>> =
        ownedSubscriptionStateFlow

    override fun dispose() {
        logger.debug("dispose()")
        resultListener = null
    }

    private fun SkuDetails.toMarketItem(): MarketItem {
        val d = this
        return MarketItem(
            d.sku,
            d.type,
            d.price,
            d.priceAmountMicros,
            d.priceCurrencyCode,
            d.originalPrice,
            d.originalPriceAmountMicros,
            d.title,
            d.description,
            d.subscriptionPeriod,
            d.freeTrialPeriod,
            d.introductoryPrice,
            d.introductoryPriceAmountMicros,
            d.introductoryPricePeriod,
            d.introductoryPriceCycles,
            d.iconUrl
        )
    }
}