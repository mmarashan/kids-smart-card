package ru.volgadev.googlebillingclientwrapper.impl

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetailsParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.volgadev.googlebillingclientwrapper.api.ItemSkuType
import ru.volgadev.googlebillingclientwrapper.api.MarketItem
import ru.volgadev.googlebillingclientwrapper.api.PaymentManager
import ru.volgadev.googlebillingclientwrapper.utils.*

internal class PaymentManagerImpl(
    private val context: Context,
    ioDispatcher: CoroutineDispatcher
) : PaymentManager {

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    private val items = HashMap<String, MarketItem>()

    private val skuIds = HashMap<ItemSkuType, List<String>>().apply {
        put(ItemSkuType.IN_APP, emptyList())
        put(ItemSkuType.SUBSCRIPTION, emptyList())
    }

    override val billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener { result, purchases ->
            Log.d(TAG, "onPurchasesUpdated($result, $purchases)")
            if (result.isOk() && purchases != null) updateState()
        }.build()

    override val ownedProducts = MutableSharedFlow<List<MarketItem>>(replay = 1)

    override val ownedSubscriptions = MutableSharedFlow<List<MarketItem>>(replay = 1)

    init {
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(TAG, "onBillingSetupFinished($billingResult)")
                if (billingResult.isOk()) updateState()
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected()")
            }
        })
    }

    override fun setProjectSkuIds(ids: List<String>, skuType: ItemSkuType) {
        Log.d(TAG, "setProjectSkuIds(); ids = $ids; type = $skuType")
        skuIds[skuType] = ids
        updateState(skuType)
    }

    override fun requestPayment(skuId: String) {
        val item = items[skuId]
        if (item == null) {
            Log.e(TAG,"Call paymentRequest() for not exist item")
            return
        }

        BillingProcessorServiceLocator.register(
            billingProcessor = billingClient,
            params = item.skuDetails.packToBillingFlowParams()
        )

        TransparentBillingClientActivity.launch(context)
    }

    override fun consumePurchase(skuId: String): Boolean {
        Log.d(TAG, "consumePurchase($skuId)")
        val item = items[skuId]
        val purchase = item?.purchase

        if (item != null && purchase != null) {
            val consumeParams = purchase.packToConsumeParams()

            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.isOk()) {
                    Log.d(TAG, "consumePurchase($skuId) OK")
                    updateState(item.skuType)
                }
            }
        }
        return false
    }

    override fun dispose() {
        Log.d(TAG, "dispose()")
        billingClient.endConnection()
        BillingProcessorServiceLocator.clear()
    }

    private fun updateState() = ItemSkuType.values().forEach { type ->
        if (skuIds[type]?.size ?: 0 > 0) updateState(type)
    }

    private fun updateState(skuType: ItemSkuType) {
        val skuIds = skuIds[skuType] ?: return
        Log.d(TAG, "updateState(); type = $skuType; skuIds = ${skuIds.joinToString()}")

        val skuDetailsParams = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuIds)
            .setType(skuType.skyType)
            .build()

        scope.launch {
            billingClient.querySkuDetailsAsync(skuDetailsParams) { result, skuDetails ->

                Log.d(TAG, "SKU detail response ${result.responseCode}")
                if (result.responseCode == 0 && skuDetails != null) {

                    skuDetails.forEach { items[it.sku] = MarketItem(it) }

                    val purchases = billingClient.queryPurchases(skuType)

                    for (purchase in purchases) {
                        purchase.skus.forEach { skuId ->
                            items[skuId]?.purchase = purchase
                        }

                        if (!purchase.isAcknowledged) {
                            Log.d(TAG, "Try to acknowledgePurchase")
                            billingClient.acknowledge(purchase) {
                                Log.d(TAG, "acknowledgePurchase result=${it.responseCode}")
                                updateState(skuType)
                            }
                        }
                    }

                    scope.launch {
                        val updated = items.values.toList()
                        Log.d(TAG, "update items ${updated.joinToString()}")
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

    companion object {
        private const val TAG = "PaymentManagerImpl"
    }
}