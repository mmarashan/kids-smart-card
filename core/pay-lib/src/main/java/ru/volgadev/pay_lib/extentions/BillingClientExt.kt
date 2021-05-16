package ru.volgadev.pay_lib.extentions

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase

fun BillingResult.isOk(): Boolean {
    return responseCode == BillingClient.BillingResponseCode.OK
}

fun ConsumeParams.build(purchase: Purchase) = ConsumeParams
    .newBuilder()
    .setPurchaseToken(purchase.purchaseToken)
    .build()

fun BillingClient.queryInAppPurchases(): List<Purchase> {
    val purchasesResult = queryPurchases(BillingClient.SkuType.INAPP)
    return purchasesResult.purchasesList ?: listOf()
}