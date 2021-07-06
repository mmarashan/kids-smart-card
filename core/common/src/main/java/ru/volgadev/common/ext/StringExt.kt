package ru.volgadev.common.ext

import android.util.Patterns
import android.webkit.URLUtil
import java.net.MalformedURLException
import java.net.URL

fun String.isValidUrl(): Boolean {
    return try {
        URL(this)
        URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this)
            .matches()
    } catch (e: MalformedURLException) {
        false
    }
}