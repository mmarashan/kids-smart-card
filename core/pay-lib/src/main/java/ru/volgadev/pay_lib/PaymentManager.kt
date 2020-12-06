package ru.volgadev.pay_lib

import android.os.Parcelable
import androidx.annotation.WorkerThread
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow
import ru.volgadev.pay_lib.impl.BillingClientActivity

@WorkerThread
interface PaymentManager {

    fun setSkuIds(ids: List<String>)

    fun requestPayment(
        paymentRequest: PaymentRequest,
        activityClass: Class<out BillingClientActivity>,
        resultListener: PaymentResultListener
    )

    fun consumePurchase(itemId: String): Boolean

    fun productsFlow(): Flow<List<MarketItem>>

    // fun ownedSubscriptionsFlow(): Flow<List<MarketItem>>

    fun dispose()
}

interface PaymentResultListener {
    fun onResult(result: RequestPaymentResult) = Unit
}

enum class RequestPaymentResult {
    SUCCESS_PAYMENT, USER_CANCELED, NOT_ALLOWED_PAYMENT_TYPE, ALREADY_PAYED, PAYMENT_ERROR
}

enum class PaymentType {
    PURCHASE, SUBSCRIPTION
}

@Parcelize
data class PaymentRequest(
    val itemId: String,
    val type: PaymentType,
    val name: String?,
    val description: String?,
    val imageUrl: String?
) : Parcelable


data class MarketItem(
    val skuDetails: SkuDetails,
    var purchase: Purchase? = null
)