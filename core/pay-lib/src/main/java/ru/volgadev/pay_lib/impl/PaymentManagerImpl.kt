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
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest


internal class PaymentManagerImpl(
    private val context: Context,
    private val googlePlayLicenseKey: String
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val itemsChannel = ConflatedBroadcastChannel<SkuDetails>()

    private val billingHandler = object : IBillingHandler {

        override fun onProductPurchased(productId: String, details: TransactionDetails?) {
            logger.debug("onProductPurchased()")
        }

        override fun onPurchaseHistoryRestored() {
            logger.debug("onPurchaseHistoryRestored()")
            val ownedProductIds = billingProcessor.listOwnedProducts()
            val ownedProducts = ArrayList<SkuDetails>()
            ownedProductIds.forEach { id ->
                billingProcessor.getSubscriptionListingDetails(id)?.let { details ->
                    ownedProducts.add(details)
                }
            }
        }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
            logger.debug("onBillingError()")
            if (errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
                // the user canceled the buy dialog
            }
        }

        override fun onBillingInitialized() {
            logger.debug("onBillingInitialized()")
            billingProcessor.loadOwnedPurchasesFromGoogle()
        }
    }

    private val billingProcessor: BillingProcessor by lazy {
        BillingProcessor(
            context,
            googlePlayLicenseKey,
            billingHandler
        )
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
        activityClass: Class<out BillingProcessorActivity>
    ) {
//        val isOneTimePurchaseSupported = billingProcessor.isOneTimePurchaseSupported
//        val isSubsUpdateSupported = billingProcessor.isSubscriptionUpdateSupported

        BillingProcessorServiceLocator.register(billingProcessor)
        BillingProcessorActivity.startActivity(context, paymentRequest, activityClass)
    }

    override fun consumePurchase(itemId: String): Boolean {
        logger.debug("consumePurchase($itemId)")
        val consumptionResult = billingProcessor.consumePurchase(itemId)
        logger.debug("consumptionResult=$consumptionResult")
        return consumptionResult
    }

    override fun purchaseItemsFlow(): Flow<SkuDetails> = itemsChannel.asFlow()

    override fun dispose() {
        logger.debug("dispose()")
        billingProcessor.release()
    }
}