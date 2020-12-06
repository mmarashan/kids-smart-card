package ru.volgadev.pay_lib.impl

import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.*


/** TODOs: - 1ч
 * 2. Реактивное обновление данных оплаты во вью
 * 3. Проброс ИД товаров
 */


@ExperimentalCoroutinesApi
internal class PaymentManagerImpl(
    private val context: Context
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val ownedProductsStateFlow = MutableStateFlow<List<MarketItem>>(listOf())
    private val ownedSubscriptionStateFlow = MutableStateFlow<List<MarketItem>>(listOf())

    private val itemsMap = HashMap<String, MarketItem>()

    private val skuIds = ArrayList<String>()// listOf("numbers_pro_ru")

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
                logger.debug("onPurchasesUpdated($billingResult, $purchases)")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    //здесь мы можем запросить информацию о товарах и покупках
                    updateState()
                }
            }
        }).enablePendingPurchases().build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {

            /**
             * Callback for setup process. This listener's onBillingSetupFinished(BillingResult)
             * method is called when the setup process is complete.
             */
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                logger.debug("onBillingSetupFinished($billingResult)")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    //здесь мы можем запросить информацию о товарах и покупках
                    updateState()
                }
            }

            override fun onBillingServiceDisconnected() {
                //сюда мы попадем если что-то пойдет не так
                logger.debug("onBillingServiceDisconnected()")
            }
        })
    }

    private fun updateState() {
        logger.debug("updateState() skuIds = ${skuIds.joinToString(",")}")

        if (skuIds.isEmpty()) return

        val param = SkuDetailsParams.newBuilder().setSkusList(skuIds)
            .setType(BillingClient.SkuType.INAPP).build()
        scope.launch {
            billingClient.querySkuDetailsAsync(
                param
            ) { billingResult, skuDetails ->
                if (billingResult.responseCode == 0 && skuDetails != null) {
                    skuDetails.forEach { skuDetails ->
                        itemsMap.put(skuDetails.sku, MarketItem(skuDetails))
                    }

                    val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    val purchasesList = purchasesResult.purchasesList ?: listOf()

                    for (i in purchasesList.indices) {
                        val purchase = purchasesList[i]
                        itemsMap.get(purchase.sku)?.purchase = purchase

                        if (!purchase.isAcknowledged) {
                            logger.debug("Try to acknowledgePurchase")
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { result ->
                                logger.debug("acknowledgePurchase result=${result.responseCode}")
                                updateState()
                            }
                        }
                    }

                    logger.debug("itemsMap=$itemsMap")

                    ownedProductsStateFlow.value = itemsMap.values.toList()
                }
            }
        }
    }

    private var resultListener: PaymentResultListener? = null

    override fun setSkuIds(ids: List<String>) {
        logger.debug("setSkuIds()")
        skuIds.clear()
        skuIds.addAll(ids)
        updateState()
    }

    override fun requestPayment(
        paymentRequest: PaymentRequest,
        activityClass: Class<out BillingClientActivity>,
        resultListener: PaymentResultListener
    ) {
        this.resultListener = resultListener
        val item = itemsMap.get(paymentRequest.itemId)
        if (item == null) {
            logger.error("Call paymentRequest() for not exist item")
            resultListener.onResult(RequestPaymentResult.PAYMENT_ERROR)
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(item.skuDetails)
            .build()
        BillingProcessorServiceLocator.register(billingClient, billingFlowParams)

        BillingClientActivity.startActivity(context, paymentRequest, activityClass)
    }

    override fun consumePurchase(itemId: String): Boolean {
        logger.debug("consumePurchase($itemId)")
        val item = itemsMap.get(itemId)

        if (item != null) {
            val token = item.purchase?.purchaseToken ?: ""
            val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(token).build()
            billingClient.consumeAsync(
                consumeParams
            ) { billingResult, purchaseToken ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    logger.debug("consumePurchase($itemId) OK")
                    updateState()
                }
            }
        }
        return false
    }

    override fun productsFlow(): StateFlow<List<MarketItem>> = ownedProductsStateFlow

//    override fun ownedSubscriptionsFlow(): StateFlow<List<MarketItem>> =
//        ownedSubscriptionStateFlow

    override fun dispose() {
        logger.debug("dispose()")
        BillingProcessorServiceLocator.clear()
        resultListener = null
    }
}