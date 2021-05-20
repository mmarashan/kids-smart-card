package ru.volgadev.googlebillingclientwrapper.impl

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import ru.volgadev.googlebillingclientwrapper.R

internal class TransparentBillingClientActivity : AppCompatActivity() {

    private val billingClient: BillingClient by lazy { BillingProcessorServiceLocator.get() }
    private val billingFlowParams: BillingFlowParams by lazy { BillingProcessorServiceLocator.getParams() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent_layout)
        launchBillingFlow()
        finish()
    }

    private fun launchBillingFlow() {
        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, TransparentBillingClientActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}