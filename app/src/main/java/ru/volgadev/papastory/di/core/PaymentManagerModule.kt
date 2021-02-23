package ru.volgadev.papastory.di.core

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentManagerFactory

@Module
class PaymentManagerModule {
    companion object {
        @Provides
        fun getPaymentManager(context: Context): PaymentManager {
            return PaymentManagerFactory.createPaymentManager(context = context)
        }
    }
}