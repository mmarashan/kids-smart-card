package ru.volgadev.pay_lib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.WalletConstants
import com.volgadev.pay_lib.R
import kotlinx.android.synthetic.main.activity_checkout.*
import org.json.JSONException
import org.json.JSONObject

/**
 * Checkout implementation for the app
 */
class PaymentActivity : Activity() {

    private lateinit var paymentsClient: PaymentsClient

    private val supportedNetworks: List<Int> = listOf(
        WalletConstants.CARD_NETWORK_OTHER,
        WalletConstants.CARD_NETWORK_VISA,
        WalletConstants.CARD_NETWORK_MASTERCARD
    )
    private val supportedMethods: List<String> = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"
    )

    val gateway: String = "example"
    val gatewayMerchantId: String = "exampleGatewayMerchantId"
    val tokenizationParameters = mapOf(
        "gateway" to gateway,
        "gatewayMerchantId" to gatewayMerchantId
    )

    private val paymentRequestsBuilder =
        PaymentRequestsBuilder(tokenizationParameters, supportedNetworks, supportedMethods)

    /**
     * Custom parameters required by the processor/gateway.
     * In many cases, your processor / gateway will only require a gatewayMerchantId.
     * Please refer to your processor's documentation for more information. The number of parameters
     * required and their names vary depending on the processor.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS
     */
    val countryCode: String = "US"
    val currencyCode: String = "USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val paymentRequest = intent.getParcelableExtra<PaymentRequest>(PAYMENT_REQUEST_EXTRA)

        paymentsClient = applicationContext.createPaymentsClient(true)
        possiblyShowGooglePayButton()

        googlePayButton.setOnClickListener { requestPayment() }
    }

    private fun possiblyShowGooglePayButton() {

        val request =
            paymentRequestsBuilder.isReadyToPayRequest(supportedNetworks, supportedMethods)

        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let { available ->
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

        val paymentDataRequest =
            paymentRequestsBuilder.paymentDataRequest(priceCents, countryCode, currencyCode)
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
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int) {
        when (statusCode) {
            WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE -> {
                Log.e(TAG, "ERROR_CODE_AUTHENTICATION_FAILURE")
            }
            WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR -> {
                Log.e(TAG, "ERROR_CODE_BUYER_ACCOUNT_ERROR")
            }
            WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED -> {
                Log.e(TAG, "ERROR_CODE_SPENDING_LIMIT_EXCEEDED")
            }
            WalletConstants.ERROR_CODE_INVALID_TRANSACTION -> {
                Log.e(TAG, "ERROR_CODE_INVALID_TRANSACTION")
            }
            WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION -> {
                Log.e(TAG, "ERROR_CODE_UNSUPPORTED_API_VERSION")
            }
            WalletConstants.ERROR_CODE_UNKNOWN -> {
                Log.e(TAG, "ERROR_CODE_UNKNOWN")
            }
            else -> {
                Log.e(TAG, "Error with unknown code $statusCode")
            }
        }
    }

    companion object {

        private val TAG = PaymentActivity.javaClass.name
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
        private const val PAYMENT_REQUEST_EXTRA = "PAYMENT_REQUEST_EXTRA"

        fun openPaymentActivity(context: Context, paymentRequest: PaymentRequest) {
            val intent = Intent(context, PaymentActivity::class.java).apply {
                putExtra(PAYMENT_REQUEST_EXTRA, paymentRequest)
            }
            context.startActivity(intent)
        }
    }
}