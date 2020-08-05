package ru.volgadev.common.log

import android.util.Log

class AndroidLoggerDelegate: LoggerDelegate {
    override fun debug(TAG: String, m: String) {
        Log.d(TAG, m)
    }

    override fun info(TAG: String, m: String) {
        Log.i(TAG, m)
    }

    override fun warn(TAG: String, m: String) {
        Log.w(TAG, m)
    }

    override fun error(TAG: String, m: String) {
        Log.e(TAG, m)
    }

}