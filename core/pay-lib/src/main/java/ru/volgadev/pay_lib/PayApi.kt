package ru.volgadev.pay_lib

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface PayApi {
    fun pay(itemId: String, price: Int)
}

@Parcelize
data class PaymentRequest(val price: Double,
                          val currency: String,
                          val title: String,
                          val description: String) : Parcelable