package ru.volgadev.pay_lib.impl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import ru.volgadev.pay_lib.PaymentRequest

open class BillingProcessorActivity : AppCompatActivity() {

    private val billingProcessor: BillingClient by lazy { BillingProcessorServiceLocator.get() }
    private val params: BillingFlowParams by lazy { BillingProcessorServiceLocator.getParams() }

    lateinit var paymentRequest: PaymentRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentRequest = intent.getParcelableExtra(PAYMENT_REQUEST_EXTRA)
            ?: throw IllegalStateException("You should open pay activity via PaymentActivity.openPaymentActivity(...)")
    }

    fun onClickPay() {
        val responseCode =
            billingProcessor.launchBillingFlow(this, params)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        finish()
    }

    override fun onDestroy() {
        billingProcessor = null
        params = null
        super.onDestroy()
    }

    companion object {
        private const val PAYMENT_REQUEST_EXTRA = "PAYMENT_REQUEST_EXTRA"

        fun startActivity(
            context: Context,
            paymentRequest: PaymentRequest,
            activityClass: Class<out BillingProcessorActivity>
        ) {
            val intent = Intent(context, activityClass).apply {
                putExtra(PAYMENT_REQUEST_EXTRA, paymentRequest)
            }
            context.startActivity(intent)
        }
    }
}