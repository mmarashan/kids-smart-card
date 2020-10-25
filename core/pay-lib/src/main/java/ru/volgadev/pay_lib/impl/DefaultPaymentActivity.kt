package ru.volgadev.pay_lib.impl

import android.os.Bundle
import kotlinx.android.synthetic.main.defalt_billing_activity.*
import ru.volgadev.pay_lib.R


class DefaultPaymentActivity : BillingProcessorActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.defalt_billing_activity)

        itemName.text = paymentRequest.name ?: skuDetails.title
        itemDescription.text = paymentRequest.description ?: skuDetails.description

        skuDetails.priceText?.let { priceText ->
            itemPrice.text = priceText
        }
        paymentRequest.imageUrl?.let { imageUrl ->
            itemImage
        }

        btnPay.setOnClickListener {
            onClickPay()
        }
    }
}