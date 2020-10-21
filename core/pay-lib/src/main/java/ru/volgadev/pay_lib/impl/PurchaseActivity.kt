package ru.volgadev.pay_lib.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import ru.volgadev.pay_lib.MerchantData
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.R


class PurchaseActivity : Activity(), IBillingHandler {

    var mBillingProcessor: BillingProcessor? = null
    private var mSingleTimePaymentButton: Button? = null
    private var mConsumabelButton: Button? = null
    private var mSubscriptionButton: Button? = null
    private var mProgress: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.billing_activity)

        val paymentRequest = intent.getParcelableExtra<PaymentRequest>(PAYMENT_REQUEST_EXTRA)
        val merchantParameters = intent.getParcelableExtra<MerchantData>(MERCHANT_DATA_EXTRA)
        val isTest = intent.getBooleanExtra(IS_TEST_EXTRA, true)
        if (paymentRequest == null || merchantParameters == null) {
            throw IllegalStateException("You should open pay activity via PaymentActivity.openPaymentActivity(...)")
        }

        mBillingProcessor = BillingProcessor(this, GPLAY_LICENSE, this)
        mProgress = findViewById(R.id.progress)
        mSingleTimePaymentButton = findViewById(R.id.btnSingleTypePayment)
        mConsumabelButton = findViewById(R.id.btnConsume)
        mSubscriptionButton = findViewById(R.id.btnSubscription)
        val isAvailable = BillingProcessor.isIabServiceAvailable(this)
        if (!isAvailable) {
            mProgress!!.visibility = View.GONE
            showMsg(getString(R.string.billing_not_available))
        } else {
            mBillingProcessor!!.initialize()
        }
        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        mSingleTimePaymentButton!!.setOnClickListener {
            mBillingProcessor!!.purchase(
                this@PurchaseActivity,
                ONE_TIME_PAYMENT
            )
        }
        mSubscriptionButton!!.setOnClickListener {
            mBillingProcessor!!.subscribe(
                this@PurchaseActivity,
                SUBSCRIPTION
            )
        }
        mConsumabelButton!!.setOnClickListener {
            val consumptionResult =
                mBillingProcessor!!.consumePurchase(ONE_TIME_PAYMENT)
            if (consumptionResult) {
                setupConsumableButtons(false)
            }
        }
    }

    override fun onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        showMsg("onBillingInitialized")
        if (mBillingProcessor!!.loadOwnedPurchasesFromGoogle()) {
            handleLoadedItems()
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
        showMsg("onProductPurchased")
        if (checkIfPurchaseIsValid(details!!.purchaseInfo)) {
            showMsg("purchase: $productId COMPLETED")
            when (productId) {
                ONE_TIME_PAYMENT -> setupConsumableButtons(true)
                SUBSCRIPTION -> setupSubscription(true)
            }
        } else {
            showMsg("fakePayment")
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
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
        mProgress!!.visibility = View.GONE
        val isOneTimePurchaseSupported = mBillingProcessor!!.isOneTimePurchaseSupported
        if (isOneTimePurchaseSupported) {
            mSingleTimePaymentButton!!.visibility = View.VISIBLE
            mConsumabelButton!!.visibility = View.VISIBLE
        } else {
            showMsg(getString(R.string.one_time_payment_not_supported))
        }
        val isSubsUpdateSupported = mBillingProcessor!!.isSubscriptionUpdateSupported
        if (isSubsUpdateSupported) {
            mSubscriptionButton!!.visibility = View.VISIBLE
        } else {
            showMsg(getString(R.string.subscription_not_supported))
        }
        setupConsumableButtons(mBillingProcessor!!.listOwnedProducts().contains(ONE_TIME_PAYMENT))
        setupSubscription(mBillingProcessor!!.listOwnedSubscriptions().contains(SUBSCRIPTION))
    }

    private fun setupConsumableButtons(isPurchased: Boolean) {
        mConsumabelButton!!.isEnabled = isPurchased
        mSingleTimePaymentButton!!.isEnabled = !isPurchased
        if (isPurchased) {
            mSingleTimePaymentButton!!.setText(R.string.already_bought)
            mConsumabelButton!!.setText(R.string.consume_one_time)
        } else {
            val details: SkuDetails? =
                mBillingProcessor!!.getPurchaseListingDetails(ONE_TIME_PAYMENT)
            mSingleTimePaymentButton!!.setText(
                getString(
                    R.string.one_time_payment_value,
                    details?.priceText ?: "100 rubley"
                )
            )
            mConsumabelButton!!.setText(R.string.not_bought_yet)
        }
    }

    private fun setupSubscription(isPurchased: Boolean) {
        mSubscriptionButton!!.isEnabled = !isPurchased
        if (isPurchased) {
            mSubscriptionButton!!.setText(R.string.already_subscribed)
        } else {
            val details: SkuDetails? =
                mBillingProcessor!!.getSubscriptionListingDetails(SUBSCRIPTION)
            mSubscriptionButton!!.text =
                getString(R.string.subscription_value, details?.priceText ?: "100 rubley")
        }
    }

    /**
     * With this PurchaseInfo a developer is able verify
     * a purchase from the google play store on his own
     * server. An example implementation of how to verify
     * a purchase you can find [here](https://github.com/mgoldsborough/google-play-in-app-billing-
      verification/blob/master/library/GooglePlay/InAppBilling/GooglePlayResponseValidator.php#L64)
     *
     * @return if purchase is valid
     */
    private fun checkIfPurchaseIsValid(purchaseInfo: PurchaseInfo): Boolean {
        // TODO as of now we assume that all purchases are valid
        return true
    }

    private fun showMsg(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!mBillingProcessor!!.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor!!.release()
        }
        super.onDestroy()
    }

    companion object {
        private const val ONE_TIME_PAYMENT = "otp"
        private const val SUBSCRIPTION = "subs"
        private const val GPLAY_LICENSE =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnjzOg5BORvyrs6yRBjTqDdSisuHfTJGYO5ciWRbNlG8T9b0WTGfFsJAVjc31TMYERFg/w+N2Ugyj5+dgE564HORY7N23tYp6p0Iv3ozC7+z5BaNMdRGMkz6tlQFu2xqYkRjj4/yIOhq7xnnIVNohfE66N6Tleov2szfSLaIQ/3UU20zlcophWXVMMg/Hxu0raYtl9jAoJKMG9jk2iC4aI2qTLD0ulmHuesiNOtCFlntVmI9eqkiPgFpf9PoIgZyyOgYZJvXT6EK4E4DYjErY7F7YM8OsEwUDsbOzZdBS+HkxmGlcJLZmRmhBRp4s1TfcIlMhyfWKAQcdx8gHLyLYzwIDAQAB"

        private const val PAYMENT_REQUEST_CODE = 991
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