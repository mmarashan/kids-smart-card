package ru.volgadev.pay_lib

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants

fun Context.createPaymentsClient(isTest: Boolean = true): PaymentsClient {
    val paymentEnvironment: Int =
        if (isTest) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

    val walletOptions = Wallet.WalletOptions.Builder()
        .setEnvironment(paymentEnvironment)
        .build()

    return Wallet.getPaymentsClient(this, walletOptions)
}