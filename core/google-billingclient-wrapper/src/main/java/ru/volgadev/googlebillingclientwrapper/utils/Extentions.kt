package ru.volgadev.googlebillingclientwrapper.utils

import com.android.billingclient.api.*
import ru.volgadev.googlebillingclientwrapper.api.ItemSkuType

fun BillingResult.isOk(): Boolean {
    return responseCode == BillingClient.BillingResponseCode.OK
}

fun Purchase.packToConsumeParams() = ConsumeParams.newBuilder()
    .setPurchaseToken(purchaseToken)
    .build()

fun SkuDetails.packToBillingFlowParams() = BillingFlowParams.newBuilder()
    .setSkuDetails(this)
    .build()

fun BillingClient.queryPurchases(skuType: ItemSkuType): List<Purchase> {
    val purchasesResult = queryPurchases(skuType.skyType)
    return purchasesResult.purchasesList ?: listOf()
}

fun BillingClient.acknowledge(
    purchase: Purchase,
    listener: AcknowledgePurchaseResponseListener
) {
    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()
    acknowledgePurchase(acknowledgePurchaseParams, listener)
}