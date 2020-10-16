package ru.volgadev.pay_lib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.volgadev.pay_lib.R
import kotlinx.android.synthetic.main.activity_checkout.*
import org.json.JSONException
import org.json.JSONObject

/**
 * Checkout implementation for the app
 */
class CheckoutActivity : Activity() {

    private lateinit var paymentsClient: PaymentsClient

    private val paymentsUtil = PaymentRequestsBuilder()

    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    private fun createPaymentsClient(isTest: Boolean = true): PaymentsClient {

        val paymentEnvironment: Int =
            if (isTest) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(paymentEnvironment)
            .build()

        return Wallet.getPaymentsClient(this, walletOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        paymentsClient = createPaymentsClient(true)
        possiblyShowGooglePayButton()

        googlePayButton.setOnClickListener { requestPayment() }
    }

    private fun possiblyShowGooglePayButton() {

        val request = paymentsUtil.isReadyToPayRequest()

        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let{ available ->
                    if (available) {
                        googlePayButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this,
                            "Unfortunately, Google Pay is not available on this device",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                }
            } catch (exception: ApiException) {
                // Process error
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    private fun requestPayment() {

        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val garmentPrice = 100.0 // selectedGarment.getDouble("price")
        val priceCents =
            Math.round(garmentPrice * CENTS.toLong())

        val paymentDataRequest = paymentsUtil.paymentDataRequest(priceCents)
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(paymentDataRequest), this, LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // Value passed in AutoResolveHelper
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK ->
                        data?.let { intent ->
                            PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                        }

                    RESULT_CANCELED -> {
                        // The user cancelled the payment attempt
                    }

                    AutoResolveHelper.RESULT_ERROR -> {
                        AutoResolveHelper.getStatusFromIntent(data)?.let {
                            handleError(it.statusCode)
                        }
                    }
                }

                // Re-enables the Google Pay payment button.
                googlePayButton.isClickable = true
            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData =
                JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)

            Toast.makeText(
                this,
                getString(R.string.payments_show_name, billingName),
                Toast.LENGTH_LONG
            ).show()

            // Logging token string.
            Log.d(
                "GooglePaymentToken", paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
            )

        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: " + e.toString())
        }

    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode))
    }

    companion object {

        val PRICE_EXTRA = "PRICE_EXTRA"
        val CURRENCY_EXTRA = "CURRENCY_EXTRA"
        val TITLE_EXTRA = "TITLE_EXTRA"

        fun openPaymentActivity(context: Context, price: Double) {
            val intent = Intent(context, CheckoutActivity::class.java).apply {
                putExtra(PRICE_EXTRA, price)
            }

            context.startActivity(intent)
        }
    }
}