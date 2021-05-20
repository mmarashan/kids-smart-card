package ru.volgadev.googlebillingclientwrapper

import android.content.Context
import androidx.annotation.AnyThread
import ru.volgadev.googlebillingclientwrapper.impl.PaymentManagerImpl

object PaymentManagerFactory {

    @AnyThread
    @JvmStatic
    fun createPaymentManager(context: Context): PaymentManager {
        return PaymentManagerImpl(context)
    }
}