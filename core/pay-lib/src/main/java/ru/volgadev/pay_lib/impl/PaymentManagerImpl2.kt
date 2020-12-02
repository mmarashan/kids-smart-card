package ru.volgadev.pay_lib.impl

import android.content.Context
import android.text.TextUtils
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.volgadev.common.log.Logger
import ru.volgadev.pay_lib.PaymentManager
import ru.volgadev.pay_lib.PaymentRequest
import ru.volgadev.pay_lib.PaymentResultListener
import ru.volgadev.pay_lib.RequestPaymentResult


@ExperimentalCoroutinesApi
internal class PaymentManagerImpl2(
    private val context: Context,
    private val googlePlayLicenseKey: String
) : PaymentManager {

    private val logger = Logger.get("PaymentManagerImpl")

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val ownedProductsStateFlow = MutableStateFlow<ArrayList<SkuDetails>>(arrayListOf())
    private val ownedSubscriptionStateFlow = MutableStateFlow<ArrayList<SkuDetails>>(arrayListOf())


    private val mBillingClient =
        BillingClient.newBuilder(context).setListener(object : PurchasesUpdatedListener {

            override fun onPurchasesUpdated(
                billingResult: BillingResult,
                purchases: MutableList<Purchase>?
            ) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    //здесь мы можем запросить информацию о товарах и покупках
                }
            }
        }).enablePendingPurchases().build()

    init {
        mBillingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(p0: BillingResult) {
                if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                    //здесь мы можем запросить информацию о товарах и покупках
                    querySkuDetails()

                    val purchasesList = queryPurchases() //запрос о покупках

                    //если товар уже куплен, предоставить его пользователю
                    for (i in purchasesList.indices) {
                        val purchaseId = purchasesList[i].sku

                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                //сюда мы попадем если что-то пойдет не так
            }
        })
    }

    private val mSkuDetailsMap = HashMap<String, SkuDetails>()

    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        val mSkuId = "test"
        skuList.add(mSkuId)
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        scope.launch {
            mBillingClient.querySkuDetailsAsync(
                skuDetailsParamsBuilder.build()
            ) { billingResult, skuDetails ->
                if (billingResult.responseCode == 0 && skuDetails != null) {
                    for (skuDetail in skuDetails) {
                        mSkuDetailsMap[skuDetail.sku] = skuDetail
                    }
                }
            }
        }
    }

    private fun queryPurchases(): List<Purchase> {
        val purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        return purchasesResult.purchasesList ?: listOf()
    }

    private var resultListener: PaymentResultListener? = null

    private fun loadOwnedItems() {
        logger.debug("loadOwnedItems()")
        val loadResult = false
        if (loadResult) {
            updateOwnedItems()
        }
    }

    private fun updateOwnedItems() {
        logger.debug("Update owned items")
        // TODO: find out why not all products
    }

    override fun isAvailable(): Boolean {
        logger.debug("isAvailable()")
        val isAvailable = false
        logger.debug("BillingProcessor.isAvailable = $isAvailable")
        return isAvailable
    }

    override fun requestPayment(
        paymentRequest: PaymentRequest,
        // TODO: maybe current activity instance
        activityClass: Class<out BillingProcessorActivity>,
        resultListener: PaymentResultListener
    ) {
        this.resultListener = resultListener
        val item = mSkuDetailsMap[paymentRequest.itemId]
        if (item == null) {
            logger.error("Call paymentRequest() for not exist item")
            resultListener.onResult(RequestPaymentResult.PAYMENT_ERROR)
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(item)
            .build()
        val responseCode = mBillingClient.launchBillingFlow(activityClass.newInstance(), billingFlowParams)
    }

    override fun consumePurchase(itemId: String): Boolean {
        logger.debug("consumePurchase($itemId)")
        val consumptionResult = false
        logger.debug("consumptionResult=$consumptionResult")
        if (consumptionResult) updateOwnedItems()
        return consumptionResult
    }

    override fun ownedProductsFlow(): StateFlow<ArrayList<SkuDetails>> = ownedProductsStateFlow

    override fun ownedSubscriptionsFlow(): StateFlow<ArrayList<SkuDetails>> =
        ownedSubscriptionStateFlow

    override fun dispose() {
        logger.debug("dispose()")
        resultListener = null
    }
}