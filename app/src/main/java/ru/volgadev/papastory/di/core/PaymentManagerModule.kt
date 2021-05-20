package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.volgadev.googlebillingclientwrapper.PaymentManager
import ru.volgadev.googlebillingclientwrapper.PaymentManagerFactory

@Module
object PaymentManagerModule {
    @Provides
    fun getPaymentManager(context: Context): PaymentManager {
        return PaymentManagerFactory.createPaymentManager(context = context)
    }
}