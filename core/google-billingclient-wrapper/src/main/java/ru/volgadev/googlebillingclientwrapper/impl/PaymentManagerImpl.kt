package ru.volgadev.googlebillingclientwrapper.impl

import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.volgadev.common.log.Logger
import ru.volgadev.googlebillingclientwrapper.MarketItem
import ru.volgadev.googlebillingclientwrapper.PaymentManager
import ru.volgadev.googlebillingclientwrapper.extentions.isOk
import ru.volgadev.googlebillingclientwrapper.extentions.packToBillingFlowParams
import ru.volgadev.googlebillingclientwrapper.extentions.packToConsumeParams
import ru.volgadev.googlebillingclientwrapper.extentions.queryInAppPurchases

internal class PaymentManagerImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val items = HashMap<String, MarketItem>()

    private val skuIds = ArrayList<String>()// listOf("numbers_pro_ru")

    private val billingClient =
        BillingClient.newBuilder(context).setListener { billingResult, purchases ->
            /**
             * Listener interface for purchase updates which happen when,
             * for example, the user buys something within the app or by initiating a purchase
             * from Google Play Store.
             */
            logger.debug("onPurchasesUpdated($billingResult, $purchases)")
            if (billingResult.isOk() && purchases != null) {
                updateState()
            }
        }.enablePendingPurchases().build()

    override val ownedProducts = MutableSharedFlow<List<MarketItem>>(replay = 1)

    override val ownedSubscriptions = MutableSharedFlow<List<MarketItem>>(replay = 1)

    init {
        billingClient.startConnection(object : BillingClientStateListener {

            /**
             * Callback for setup process. This listener's onBillingSetupFinished(BillingResult)
             * method is called when the setup process is complete.
             */
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                logger.debug("onBillingSetupFinished($billingResult)")
                if (billingResult.isOk()) updateState()
            }

            override fun onBillingServiceDisconnected() {
                //сюда мы попадем если что-то пойдет не так
                logger.debug("onBillingServiceDisconnected()")
            }
        })
    }

    override fun dispose() {
        logger.debug("dispose()")
        billingClient.endConnection()
        BillingProcessorServiceLocator.clear()
    }

    override fun setProjectSkuIds(ids: List<String>) {
        logger.debug("setSkuIds()")
        skuIds.clear()
        skuIds.addAll(ids)
        updateState()
    }

    override fun requestPayment(skuId: String) {
        val item = items[skuId]
        if (item == null) {
            logger.error("Call paymentRequest() for not exist item")
            return
        }

        BillingProcessorServiceLocator.register(
            billingProcessor = billingClient,
            params = item.skuDetails.packToBillingFlowParams()
        )

        TransparentBillingClientActivity.launch(context)
    }

    override fun consumePurchase(skuId: String): Boolean {
        logger.debug("consumePurchase($skuId)")
        val item = items[skuId]
        val purchase = item?.purchase

        if (item != null && purchase != null) {
            val consumeParams = purchase.packToConsumeParams()

            billingClient.consumeAsync(
                consumeParams
            ) { billingResult, _ ->
                if (billingResult.isOk()) {
                    logger.debug("consumePurchase($skuId) OK")
                    updateState()
                }
            }
        }
        return false
    }

    private fun updateState() {
        logger.debug("updateState() skuIds = ${skuIds.joinToString(",")}")

        if (skuIds.isEmpty()) return

        val skuDetailsParams = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuIds)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        scope.launch {
            billingClient.querySkuDetailsAsync(skuDetailsParams) { result, skuDetails ->

                logger.debug("SKU detail response ${result.responseCode} ${result.debugMessage}")
                if (result.responseCode == 0 && skuDetails != null) {

                    skuDetails.forEach { items[it.sku] = MarketItem(it) }

                    val purchases = billingClient.queryInAppPurchases()

                    for (i in purchases.indices) {
                        val purchase = purchases[i]
                        items[purchase.sku]?.purchase = purchase

                        if (!purchase.isAcknowledged) acknowledgePurchase(purchase)
                    }

                    scope.launch {
                        ownedProducts.emit(items.values.toList())
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        logger.debug("Try to acknowledgePurchase")
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
            logger.debug("acknowledgePurchase result=${it.responseCode}")
            updateState()
        }
    }
}