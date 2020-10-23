package ru.volgadev.pay_lib.impl

import com.anjlab.android.iab.v3.BillingProcessor


object BillingProcessorServiceLocator {
    private var billingProcessor: BillingProcessor? = null

    fun register(billingProcessor: BillingProcessor) {
        this.billingProcessor = billingProcessor
    }

    fun get(): BillingProcessor {
        val bp = billingProcessor
        if (bp != null) {
            return bp
        } else {
            throw IllegalStateException("billingProcessor not registered!")
        }
    }
}