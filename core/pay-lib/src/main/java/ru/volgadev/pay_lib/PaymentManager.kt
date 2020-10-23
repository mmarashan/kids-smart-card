package ru.volgadev.pay_lib

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.Flow

interface PaymentManager {

    fun requestPayment(
        paymentRequest: PaymentRequest,
        isTest: Boolean = true
    )

    fun purchaseItemsFlow(): Flow<Item>
}

data class Item(val id: String)

@Parcelize
data class PaymentRequest(
    val itemId: String
) : Parcelable