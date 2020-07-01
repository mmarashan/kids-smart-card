package ru.volgadev.livebanner.common.util

import android.os.Handler
import android.os.HandlerThread

fun getBackgroundHandler(threadName: String): Handler {
    val thread = HandlerThread(threadName).apply {
        start()
    }
    return Handler(thread.looper)
}