package ru.volgadev.pay_lib.impl

import android.content.Context
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.TransactionDetails
import kotlinx.coroutines.flow.Flow
import ru.volgadev.pay_lib.Item
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest

internal class PaymentManagerImpl(val context: Context, val googlePlayLicenseKey: String) :
    PaymentManager {

    private val billingHandler = object : IBillingHandler {
        override fun onProductPurchased(productId: String, details: TransactionDetails?) {
            TODO("Not yet implemented")
        }

        override fun onPurchaseHistoryRestored() {
            TODO("Not yet implemented")
        }

        override fun onBillingError(errorCode: Int, error: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun onBillingInitialized() {
            TODO("Not yet implemented")
        }
    }

    private val billingProcessor: BillingProcessor by lazy {
        BillingProcessor(
            context,
            googlePlayLicenseKey,
            billingHandler
        )
    }

    override fun requestPayment(
        paymentRequest: PaymentRequest,
        isTest: Boolean
    ) {
        BillingProcessorServiceLocator.register(billingProcessor)
        PurchaseActivity.openPaymentActivity(context, paymentRequest, isTest)
    }

    override fun purchaseItemsFlow(): Flow<Item> {
        TODO("Not yet implemented")
    }

}