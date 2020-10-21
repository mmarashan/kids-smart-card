package ru.volgadev.pay_lib

import android.content.Context
import ru.volgadev.pay_lib.impl.PurchaseActivity

internal class PaymentManagerImpl(val context: Context) : PaymentManager {

    override fun requestPayment(
        merchantData: MerchantData,
        paymentRequest: PaymentRequest,
        isTest: Boolean
    ) {
        PurchaseActivity.openPaymentActivity(context, merchantData, paymentRequest, isTest)
    }

    override fun isPayed(): Boolean {
        return false
    }
}