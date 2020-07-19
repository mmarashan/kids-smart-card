package ru.sberdevices.user_device_data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import ru.volgadev.common.log.Logger
import java.util.*

class UserDeviceInfoRepositoryImpl(private val context: Context) : UserDeviceInfoRepository {

    private val logger = Logger.get("UserDeviceInfoRepositoryImpl")

    @ExperimentalCoroutinesApi
    override fun networkConnected(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var lastValue = false
        var wasCheck = false

        fun setValue(connected: Boolean) {
            synchronized(lastValue) {
                if ((!isClosedForSend && (connected != lastValue)) || (!wasCheck)) {
                    wasCheck = true
                    logger.debug( "Network connected - $connected ${this}" )
                    lastValue = connected
                    offer(connected)
                }
            }
        }

        connectivityManager.run {

            fun check() {
                if (activeNetworkInfo == null) {
                    logger.warn ("No active Network Info" )
                    setValue(false)
                } else {
                    val isConnected: Boolean = activeNetworkInfo.isConnected
                    setValue(isConnected)
                }
            }

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    check()
                }

                override fun onLost(network: Network?) {
                    logger.warn ("Disconnected from network" )
                    setValue(false)
                }
            }

            registerDefaultNetworkCallback(networkCallback)

            check()

            awaitClose { unregisterNetworkCallback(networkCallback) }
        }
    }

    override fun userLocale(): Flow<Locale> = flowOf(Locale.getDefault())
}