package ru.volgadev.googlebillingclientwrapper

import androidx.annotation.WorkerThread
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import kotlinx.coroutines.flow.SharedFlow

@WorkerThread
interface PaymentManager {

    fun setProjectSkuIds(ids: List<String>)

    fun requestPayment(skuId: String)

    fun consumePurchase(skuId: String): Boolean

    val ownedProducts: SharedFlow<List<MarketItem>>

    val ownedSubscriptions: SharedFlow<List<MarketItem>>

    fun dispose()
}

data class MarketItem(
    val skuDetails: SkuDetails,
    var purchase: Purchase? = null
) {
    fun isPurchased(): Boolean = purchase?.purchaseState == Purchase.PurchaseState.PURCHASED
}