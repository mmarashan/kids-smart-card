package ru.volgadev.googlebillingclientwrapper.api

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import kotlinx.coroutines.flow.SharedFlow

interface PaymentManager {

    fun setProjectSkuIds(ids: List<String>, skuType: ItemSkuType)

    fun requestPayment(skuId: String)

    fun consumePurchase(skuId: String): Boolean

    val ownedProducts: SharedFlow<List<MarketItem>>

    val ownedSubscriptions: SharedFlow<List<MarketItem>>

    fun dispose()

    val billingClient: BillingClient
}

data class MarketItem(
    val skuDetails: SkuDetails,
    var purchase: Purchase? = null
) {
    fun isPurchased(): Boolean = purchase?.purchaseState == Purchase.PurchaseState.PURCHASED

    val skuType: ItemSkuType =
        ItemSkuType.values().firstOrNull { it.skyType == skuDetails.type } ?: ItemSkuType.UNKNOWN
}

enum class ItemSkuType(val skyType: String) {
    IN_APP("inapp"),
    SUBSCRIPTION("subs"),
    /* этого результатата быть не должно, однако введен для избежания неявных ошибкок */
    UNKNOWN("")
}