package ru.volgadev.googlebillingclientwrapper.impl

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetailsParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.volgadev.common.log.Logger
import ru.volgadev.googlebillingclientwrapper.api.ItemSkuType
import ru.volgadev.googlebillingclientwrapper.api.MarketItem
import ru.volgadev.googlebillingclientwrapper.api.PaymentManager
import ru.volgadev.googlebillingclientwrapper.utils.*

internal class PaymentManagerImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val items = HashMap<String, MarketItem>()

    private val skuIds = HashMap<ItemSkuType, List<String>>().apply {
        put(ItemSkuType.IN_APP, emptyList())
        put(ItemSkuType.SUBSCRIPTION, emptyList())
    }

    private val billingClient =
        BillingClient.newBuilder(context).setListener { billingResult, purchases ->

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

    override fun setProjectSkuIds(ids: List<String>, skuType: ItemSkuType) {
        logger.debug("setProjectSkuIds(); ids = $ids; type = $skuType")
        skuIds[skuType] = ids
        updateState(skuType)
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
                    updateState(item.skuType)
                }
            }
        }
        return false
    }

    override fun dispose() {
        logger.debug("dispose()")
        billingClient.endConnection()
        BillingProcessorServiceLocator.clear()
    }

    private fun updateState() {
        ItemSkuType.values().forEach { type ->
            if (skuIds[type]?.size ?: 0 > 0) updateState(type)
        }
    }

    private fun updateState(skuType: ItemSkuType) {
        val skuIds = skuIds[skuType] ?: emptyList()
        logger.debug("updateState(); type = $skuType; skuIds = ${skuIds.joinToString(",")}")
        if (skuIds.isEmpty()) return

        val skuDetailsParams = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuIds)
            .setType(skuType.skyType)
            .build()

        scope.launch {
            billingClient.querySkuDetailsAsync(skuDetailsParams) { result, skuDetails ->

                logger.debug("SKU detail response ${result.responseCode}")
                if (result.responseCode == 0 && skuDetails != null) {

                    skuDetails.forEach { items[it.sku] = MarketItem(it) }

                    val purchases = billingClient.queryPurchases(skuType)

                    for (purchase in purchases) {
                        items[purchase.sku]?.purchase = purchase

                        if (!purchase.isAcknowledged) {
                            logger.debug("Try to acknowledgePurchase")
                            billingClient.acknowledge(purchase) {
                                logger.debug("acknowledgePurchase result=${it.responseCode}")
                                updateState(skuType)
                            }
                        }
                    }

                    scope.launch {
                        val updated = items.values.toList()
                        logger.debug("update items ${updated.joinToString()}")
                        when (skuType) {
                            ItemSkuType.IN_APP -> ownedProducts.emit(updated)
                            ItemSkuType.SUBSCRIPTION -> ownedSubscriptions.emit(updated)
                            ItemSkuType.UNKNOWN -> Unit
                        }
                    }
                }
            }
        }
    }
}