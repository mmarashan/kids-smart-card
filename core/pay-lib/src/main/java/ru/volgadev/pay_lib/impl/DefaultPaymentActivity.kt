package ru.volgadev.pay_lib.impl

import android.os.Bundle
import coil.load
import kotlinx.android.synthetic.main.default_billing_activity.*
import ru.volgadev.pay_lib.R

class DefaultPaymentActivity : BillingProcessorActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.default_billing_activity)

        itemName.text = paymentRequest.name
        itemDescription.text = paymentRequest.description

        params.skuDetails.price?.let { priceText ->
            itemPrice.text = priceText
        }
        paymentRequest.imageUrl?.let { imageUrl ->
            itemImage.load(imageUrl){
                crossfade(true)
            }
        }

        btnPay.setOnClickListener {
            onClickPay()
        }
    }
}