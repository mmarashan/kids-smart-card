package ru.volgadev.pay_lib

import android.os.Parcelable
import com.anjlab.android.iab.v3.SkuDetails
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow
import ru.volgadev.pay_lib.impl.BillingProcessorActivity

interface PaymentManager {

    fun init(): Boolean

    fun requestPayment(
        paymentRequest: PaymentRequest,
        activityClass: Class<out BillingProcessorActivity>
    )

    fun consumePurchase(itemId: String): Boolean

    fun purchaseItemsFlow(): Flow<SkuDetails>

    fun dispose()
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