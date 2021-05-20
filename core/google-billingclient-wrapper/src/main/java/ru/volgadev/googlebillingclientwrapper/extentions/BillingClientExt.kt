package ru.volgadev.googlebillingclientwrapper.extentions

import com.android.billingclient.api.*

fun BillingResult.isOk(): Boolean {
    return responseCode == BillingClient.BillingResponseCode.OK
}

fun Purchase.packToConsumeParams() = ConsumeParams.newBuilder()
    .setPurchaseToken(purchaseToken)
    .build()

fun SkuDetails.packToBillingFlowParams() = BillingFlowParams.newBuilder()
    .setSkuDetails(this)
    .build()


fun BillingClient.queryInAppPurchases(): List<Purchase> {
    val purchasesResult = queryPurchases(BillingClient.SkuType.INAPP)
    return purchasesResult.purchasesList ?: listOf()
}