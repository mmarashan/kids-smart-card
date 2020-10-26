package ru.volgadev.pay_lib.impl

import android.content.Context
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.Constants
import com.anjlab.android.iab.v3.SkuDetails
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.*


internal class PaymentManagerImpl(
    private val context: Context,
    private val googlePlayLicenseKey: String
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val ownedProductsChannel = ConflatedBroadcastChannel<ArrayList<SkuDetails>>()
    private val ownedSubscriptionChannel = ConflatedBroadcastChannel<ArrayList<SkuDetails>>()

    private val billingHandler = object : IBillingHandler {

        override fun onProductPurchased(productId: String, details: TransactionDetails?) {
            logger.debug("onProductPurchased($productId)")
            resultListener?.onResult(RequestPaymentResult.SUCCESS_PAYMENT)
            updateOwnedItems()
        }

        override fun onPurchaseHistoryRestored() {
            logger.debug("onPurchaseHistoryRestored()")
            updateOwnedItems()
        }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
            logger.warn("onBillingError()")
            if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
                logger.warn("User canceled the payment dialog")
                updateOwnedItems()
                resultListener?.onResult(RequestPaymentResult.USER_CANCELED)
            }
            resultListener?.onResult(RequestPaymentResult.PAYMENT_ERROR)
        }

        override fun onBillingInitialized() {
            logger.debug("onBillingInitialized()")
            loadOwnedItems()
        }
    }

    private val billingProcessor = BillingProcessor(
        context,
        googlePlayLicenseKey,
        billingHandler
    )

    private var resultListener: PaymentResultListener? = null

    private fun loadOwnedItems() {
        logger.debug("loadOwnedItems()")
        val loadResult = billingProcessor.loadOwnedPurchasesFromGoogle()
        if (loadResult) {
            updateOwnedItems()
        }
    }

    private fun updateOwnedItems() {
        logger.debug("Update owned items")
        val ownedProductIds = billingProcessor.listOwnedProducts() as ArrayList<String>
        val ownedSubscriptionIds = billingProcessor.listOwnedSubscriptions() as ArrayList<String>

        val ownedProducts =
            billingProcessor.getPurchaseListingDetails(ownedProductIds) as ArrayList<SkuDetails>?
                ?: ArrayList()
        val ownedSubscriptions =
            billingProcessor.getSubscriptionListingDetails(ownedSubscriptionIds) as ArrayList<SkuDetails>?
                ?: ArrayList()

        logger.debug("ownedProducts = ${ownedProducts.joinToString(",")}")
        logger.debug("ownedSubscription = ${ownedSubscriptions.joinToString(",")}")
        ownedProductsChannel.offer(ownedProducts)
        ownedSubscriptionChannel.offer(ownedSubscriptions)
    }

    override fun init(): Boolean {
        logger.debug("init()")
        val isAvailable = BillingProcessor.isIabServiceAvailable(context)
        logger.debug("BillingProcessor.isAvailable = $isAvailable")
        return if (!isAvailable) {
            false
        } else {
            billingProcessor.initialize()
            true
        }
    }

    override fun requestPayment(
        paymentRequest: PaymentRequest,
        activityClass: Class<out BillingProcessorActivity>,
        resultListener: PaymentResultListener
    ) {
        this.resultListener = resultListener
        val isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported
        val isSubsUpdateSupported = billingProcessor.isSubscriptionUpdateSupported

        if ((isOneTimePurchaseSupported && paymentRequest.type == PaymentType.PURCHASE)
            || isSubsUpdateSupported && paymentRequest.type == PaymentType.SUBSCRIPTION
        ) {
            logger.error("Payment not supported type = ${paymentRequest.type} ")
            resultListener.onResult(RequestPaymentResult.NOT_ALLOWED_PAYMENT_TYPE)
            return
        }

        if (paymentRequest.type == PaymentType.PURCHASE) {
            val transactionDetails =
                billingProcessor.getPurchaseTransactionDetails(paymentRequest.itemId)
            if (transactionDetails != null) {
                logger.warn("Payment already payed ${paymentRequest.type} ")
                resultListener.onResult(RequestPaymentResult.ALREADY_PAYED)
                return
            }
        }

        BillingProcessorServiceLocator.register(billingProcessor)
        BillingProcessorActivity.startActivity(context, paymentRequest, activityClass)
    }

    override fun consumePurchase(itemId: String): Boolean {
        logger.debug("consumePurchase($itemId)")
        val consumptionResult = billingProcessor.consumePurchase(itemId)
        logger.debug("consumptionResult=$consumptionResult")
        return consumptionResult
    }

    override fun ownedProductsFlow(): Flow<ArrayList<SkuDetails>> = ownedProductsChannel.asFlow()

    override fun ownedSubscriptionsFlow(): Flow<ArrayList<SkuDetails>> =
        ownedSubscriptionChannel.asFlow()

    override fun dispose() {
        logger.debug("dispose()")
        billingProcessor.release()
    }
}