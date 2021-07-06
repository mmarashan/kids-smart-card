package ru.volgadev.common.ext

import android.app.Activity
import android.view.View
import android.view.Window

fun Activity.hideNavBar() {
    window.hideNavBar()
}

fun Activity.showNavBar() {
    window.showNavBar()
}

fun Window.hideNavBar() {
    val decorView: View = this.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    decorView.systemUiVisibility = uiOptions
}

fun Window.showNavBar() {
    val decorView: View = this.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = uiOptions
}
