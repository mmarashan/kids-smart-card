package ru.volgadev.pay_lib

import android.os.Parcelable
import androidx.annotation.WorkerThread
import com.android.billingclient.api.SkuDetails
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow
import ru.volgadev.pay_lib.impl.BillingProcessorActivity

@WorkerThread
interface PaymentManager {

    fun isAvailable(): Boolean

    fun requestPayment(
        paymentRequest: PaymentRequest,
        activityClass: Class<out BillingProcessorActivity>,
        resultListener: PaymentResultListener
    )

    fun consumePurchase(itemId: String): Boolean

    fun ownedProductsFlow(): Flow<List<MarketItem>>

    fun ownedSubscriptionsFlow(): Flow<List<MarketItem>>

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
    val productId: String,
    val type: String,
    val price: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val originalPrice: String,
    val originalPriceAmountMicros: Long?,
    val title: String,
    val description: String,
    val subscriptionPeriod: String,
    val freeTrialPeriod: String,
    val introductoryPrice: String,
    val introductoryPriceAmountMicros: Long?,
    val introductoryPricePeriod: String,
    val introductoryPriceCycles: Int,
    val iconUrl: String
)