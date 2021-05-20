package ru.volgadev.googlebillingclientwrapper.impl

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams

internal object BillingProcessorServiceLocator {
    private var billingClient: BillingClient? = null
    private var billingFlowParams: BillingFlowParams? = null

    fun register(billingProcessor: BillingClient, params: BillingFlowParams) {
        this.billingClient = billingProcessor
        this.billingFlowParams = params
    }

    @JvmStatic
    fun get(): BillingClient {
        val bp = billingClient
        if (bp != null) {
            return bp
        } else {
            throw IllegalStateException("billingProcessor not registered!")
        }
    }

    @JvmStatic
    fun getParams(): BillingFlowParams {
        val bp = billingFlowParams
        if (bp != null) {
            return bp
        } else {
            throw IllegalStateException("billingProcessor not registered!")
        }
    }

    @JvmStatic
    fun clear() {
        billingClient = null
        billingFlowParams = null
    }
}