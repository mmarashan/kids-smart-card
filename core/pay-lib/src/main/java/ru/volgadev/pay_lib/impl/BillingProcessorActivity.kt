package ru.volgadev.pay_lib.impl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentType

open class BillingProcessorActivity : AppCompatActivity() {

    private val billingProcessor: BillingProcessor by lazy { BillingProcessorServiceLocator.get() }

    private lateinit var paymentRequest: PaymentRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentRequest = intent.getParcelableExtra<PaymentRequest>(PAYMENT_REQUEST_EXTRA)
            ?: throw IllegalStateException("You should open pay activity via PaymentActivity.openPaymentActivity(...)")

    }

    fun onClickPay() {
        val paymentId = paymentRequest.itemId
        val type = paymentRequest.type
        if (type == PaymentType.PURCHASE) {
            billingProcessor.purchase(
                this,
                paymentId
            )
        } else {
            billingProcessor.subscribe(
                this,
                paymentId
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
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