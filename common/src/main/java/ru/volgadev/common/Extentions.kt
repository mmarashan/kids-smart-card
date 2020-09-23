package ru.volgadev.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.File
import java.net.MalformedURLException


fun Context.applicationDataDir(): String {
    val p: PackageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
    return p.applicationInfo.dataDir + "/"
}

fun Context.isPermissionGranted(permission: String): Boolean {
    val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
    return permissionCheck == PackageManager.PERMISSION_GRANTED
}

fun Drawable.toBitmap(): Bitmap {
    val drawable = this
    if (drawable is BitmapDrawable) {
        return (drawable as BitmapDrawable).bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Activity.hideNavBar() {
    val decorView: View = window.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    decorView.systemUiVisibility = uiOptions
}

fun Activity.showNavBar() {
    val decorView: View = window.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    decorView.systemUiVisibility = uiOptions
}

fun View.setVisibleWithTransition(
    visibility: Int,
    transition: Transition,
    durationMs: Long,
    parent: ViewGroup,
    delayMs: Long = 0L
) {
    transition.duration = durationMs
    transition.startDelay = delayMs
    transition.addTarget(this)
    TransitionManager.beginDelayedTransition(parent, transition)
    this.visibility = visibility
}


fun View.runLevitateAnimation(amplitudeY: Float, duration: Long) {
    val interpolator: TimeInterpolator = DecelerateInterpolator()
    this.animate().translationYBy(amplitudeY).setDuration(duration)
        .setInterpolator(interpolator).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                runLevitateAnimation(-amplitudeY, duration)
            }
        })
}

fun View.runScaleAnimation(amplitudeZ: Float, period: Long, count: Int) {
    assert(count > 0)
    assert(amplitudeZ > 0)
    assert(amplitudeZ <= 1)
    val interpolator: TimeInterpolator = DecelerateInterpolator()
    this.animate().scaleX(amplitudeZ).scaleY(amplitudeZ).setDuration(period)
        .setInterpolator(interpolator).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                if (count > 1) {
                    runScaleAnimation(1/amplitudeZ, period, count - 1)
                } else {
                    runScaleAnimation(1f, period, 0)
                }
            }
        })
}

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun String.isValidUrlString(): Boolean {
    try {
        return this.isNotEmpty() && URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this)
            .matches()
    } catch (e: MalformedURLException) {
    }
    return false
}

fun File.isExistsNonEmptyFile(): Boolean {
    return isFile && exists() && length() > 0
}