package ru.volgadev.pay_lib

import android.os.Parcelable
import androidx.annotation.WorkerThread
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.SharedFlow
import ru.volgadev.pay_lib.impl.BillingClientActivity

@WorkerThread
interface PaymentManager {

    fun setSkuIds(ids: List<String>)

    fun requestPayment(
        paymentRequest: PaymentRequest,
        activityClass: Class<out BillingClientActivity>
    )

    fun consumePurchase(skuId: String): Boolean

    val ownedProducts: SharedFlow<List<MarketItem>>

    val ownedSubscriptions: SharedFlow<List<MarketItem>>

    fun dispose()
}

@Parcelize
data class PaymentRequest(
    val skuId: String,
    val name: String?,
    val description: String?,
    val imageUrl: String?
) : Parcelable


data class MarketItem(
    val skuDetails: SkuDetails,
    var purchase: Purchase? = null
) {
    fun isPurchased(): Boolean = purchase?.purchaseState == Purchase.PurchaseState.PURCHASED
}