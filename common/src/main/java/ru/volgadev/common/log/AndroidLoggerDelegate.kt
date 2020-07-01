package ru.volgadev.livebanner.common.log

import android.util.Log

class AndroidLoggerDelegate(val TAG: String): LoggerDelegate {
    override fun debug(m: String) {
        Log.d(TAG, m)
    }

    override fun info(m: String) {
        Log.i(TAG, m)
    }

    override fun warn(m: String) {
        Log.w(TAG, m)
    }

    override fun error(m: String) {
        Log.e(TAG, m)
    }

}