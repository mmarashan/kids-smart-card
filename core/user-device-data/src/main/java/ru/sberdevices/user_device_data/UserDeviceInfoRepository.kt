package ru.sberdevices.user_device_data

import kotlinx.coroutines.flow.Flow
import java.util.*

interface UserDeviceInfoRepository {
    fun networkConnected(): Flow<Boolean>
    fun userLocale(): Flow<Locale>
}