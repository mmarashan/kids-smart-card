package ru.volgadev.pay_lib.impl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.*
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import kotlinx.android.synthetic.main.billing_activity.*
import ru.volgadev.pay_lib.MerchantData
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.R


class PurchaseActivity : AppCompatActivity(R.layout.billing_activity), IBillingHandler {

    private val billingProcessor: BillingProcessor by lazy { BillingProcessor(this, GOOGLE_PLAY_LICENSE_KEY, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentRequest = intent.getParcelableExtra<PaymentRequest>(PAYMENT_REQUEST_EXTRA)
        val merchantParameters = intent.getParcelableExtra<MerchantData>(MERCHANT_DATA_EXTRA)
        val isTest = intent.getBooleanExtra(IS_TEST_EXTRA, true)
        if (paymentRequest == null || merchantParameters == null) {
            throw IllegalStateException("You should open pay activity via PaymentActivity.openPaymentActivity(...)")
        }

        val isAvailable = BillingProcessor.isIabServiceAvailable(this)
        if (!isAvailable) {
            progress.visibility = View.GONE
            showMsg(getString(R.string.billing_not_available))
        } else {
            billingProcessor.initialize()
        }

        btnSingleTypePayment.setOnClickListener {
            billingProcessor.purchase(
                this@PurchaseActivity,
                ONE_TIME_PAYMENT
            )
        }
        btnSubscription.setOnClickListener {
            billingProcessor.subscribe(
                this@PurchaseActivity,
                SUBSCRIPTION
            )
        }
        btnConsume.setOnClickListener {
            val consumptionResult = billingProcessor.consumePurchase(ONE_TIME_PAYMENT)
            if (consumptionResult) {
                setupConsumableButtons(false)
            }
        }
    }

    override fun onBillingInitialized() {
        showMsg("onBillingInitialized")
        if (billingProcessor.loadOwnedPurchasesFromGoogle()) {
            handleLoadedItems()
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        showMsg("onProductPurchased: $productId COMPLETED")
        when (productId) {
            ONE_TIME_PAYMENT -> setupConsumableButtons(true)
            SUBSCRIPTION -> setupSubscription(true)
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED){
            // the user canceled the buy dialog
        }
        showMsg("onBillingError")
    }

    override fun onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
        showMsg("onPurchaseHistoryRestored")
        handleLoadedItems()
    }

    private fun handleLoadedItems() {
        progress.visibility = View.GONE
        val isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported
        if (isOneTimePurchaseSupported) {
            btnSingleTypePayment.visibility = View.VISIBLE
            btnConsume.visibility = View.VISIBLE
        } else {
            showMsg(getString(R.string.one_time_payment_not_supported))
        }
        val isSubsUpdateSupported = billingProcessor.isSubscriptionUpdateSupported
        if (isSubsUpdateSupported) {
            btnSubscription.visibility = View.VISIBLE
        } else {
            showMsg(getString(R.string.subscription_not_supported))
        }
        val ownerButtons = billingProcessor.listOwnedProducts()
        setupConsumableButtons(ownerButtons.contains(ONE_TIME_PAYMENT))
        setupSubscription(ownerButtons.contains(SUBSCRIPTION))
    }

    private fun setupConsumableButtons(isPurchased: Boolean) {
        btnConsume.isEnabled = isPurchased
        btnSingleTypePayment.isEnabled = !isPurchased
        if (isPurchased) {
            btnSingleTypePayment.setText(R.string.already_bought)
            btnConsume.setText(R.string.consume_one_time)
        } else {
            val details: SkuDetails? =
                billingProcessor.getPurchaseListingDetails(ONE_TIME_PAYMENT)
            btnSingleTypePayment.text = getString(
                R.string.one_time_payment_value,
                details?.priceText ?: "100 rubley"
            )
            btnConsume.setText(R.string.not_bought_yet)
        }
    }

    private fun setupSubscription(isPurchased: Boolean) {
        btnSubscription.isEnabled = !isPurchased
        if (isPurchased) {
            btnSubscription.setText(R.string.already_subscribed)
        } else {
            val details: SkuDetails? =
                billingProcessor.getSubscriptionListingDetails(SUBSCRIPTION)
            btnSubscription.text =
                getString(R.string.subscription_value, details?.priceText ?: "100 rubley")
        }
    }

    private fun showMsg(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    companion object {
        private const val ONE_TIME_PAYMENT = "test_set"
        private const val SUBSCRIPTION = "subs"
        private const val GOOGLE_PLAY_LICENSE_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiy7rUiTQjax6lXC1p++/aGn7PVDImiIinlc0LASZ6aLvQ0Hp3oRulwt/Zk4CxU8cuI4F4jfAfTYxJg07VpagC+fnknWFkSfl9aP+dsFrxHdSr1U9k05/GpYTTBNVj8zQUVIPvZ9ZHYtO+CQyokwK9UWVzOWFtMKZ0d4DZ5dcm8nA1c2PgO10nOReAmJ3NMMxaFYeiyhCV6gZMgryXREboLV8nfGC+YpFbldC4qpSJFWvrJPHuE+mDneYwlSYqyk/dPg23U8a4T7tCiskWlT+JAr9KBr1jd5gfOV4bN76xsh/W4ic/VhA7w6AR4TM4BT6QN24q/DUvCDOYpVPnjbZhwIDAQAB"

        private const val MERCHANT_DATA_EXTRA = "MERCHANT_PARAMETERS_EXTRA"
        private const val PAYMENT_REQUEST_EXTRA = "PAYMENT_REQUEST_EXTRA"
        private const val IS_TEST_EXTRA = "IS_TEST_EXTRA"

        fun openPaymentActivity(
            context: Context,
            merchantData: MerchantData,
            paymentRequest: PaymentRequest,
            isTest: Boolean = true
        ) {
            val intent = Intent(context, PurchaseActivity::class.java).apply {
                putExtra(PAYMENT_REQUEST_EXTRA, paymentRequest)
                putExtra(MERCHANT_DATA_EXTRA, merchantData)
                putExtra(IS_TEST_EXTRA, isTest)
            }
            context.startActivity(intent)
        }
    }
}