package ru.volgadev.pay_lib

import android.os.Parcelable
import androidx.annotation.WorkerThread
import com.anjlab.android.iab.v3.SkuDetails
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

    fun ownedProductsFlow(): Flow<ArrayList<SkuDetails>>

    fun ownedSubscriptionsFlow(): Flow<ArrayList<SkuDetails>>

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