package ru.volgadev.pay_lib.impl

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams

object BillingProcessorServiceLocator {
    private var billingProcessor: BillingClient? = null
    private var params: BillingFlowParams? = null

    fun register(billingProcessor: BillingClient, params: BillingFlowParams) {
        this.billingProcessor = billingProcessor
        this.params = params
    }

    @JvmStatic
    fun get(): BillingClient {
        val bp = billingProcessor
        if (bp != null) {
            return bp
        } else {
            throw IllegalStateException("billingProcessor not registered!")
        }
    }

    @JvmStatic
    fun getParams(): BillingFlowParams {
        val bp = params
        if (bp != null) {
            return bp
        } else {
            throw IllegalStateException("billingProcessor not registered!")
        }
    }

    @JvmStatic
    fun clear(){
        billingProcessor = null
        params = null
    }
}