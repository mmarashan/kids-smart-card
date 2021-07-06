package ru.volgadev.common.logger

import android.util.Log

class AndroidLoggerDelegate: LoggerDelegate {
    override fun debug(tag: String, m: String) {
        Log.d(tag, m)
    }

    override fun info(tag: String, m: String) {
        Log.i(tag, m)
    }

    override fun warn(tag: String, m: String) {
        Log.w(tag, m)
    }

    override fun error(tag: String, m: String) {
        Log.e(tag, m)
    }
}