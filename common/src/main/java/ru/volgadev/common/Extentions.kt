package ru.volgadev.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.File
import java.net.MalformedURLException
import kotlin.math.min


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

fun View.scaleToFitAnimatedAndBack(
    timeUp: Long,
    timeDelay: Long,
    timeDown: Long,
    scaleRate: Float = 1f,
    onEnd: () -> Unit = {}
) {
    val animPair = scaleToFitParentAnimation(this, scaleRate)
    val animScaleUp = animPair.first
    val animScaleDown = animPair.second
    val view = this
    animScaleUp.duration = timeUp
    animScaleUp.fillAfter = true
    animScaleUp.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            animScaleDown.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    onEnd.invoke()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            animScaleDown.duration = timeDown
            view.postDelayed({
                view.startAnimation(animScaleDown)
            }, timeDelay)
        }

        override fun onAnimationRepeat(animation: Animation?) {}
    })
    this.startAnimation(animScaleUp)
}

private fun scaleToFitParentAnimation(view: View, scaleRate: Float = 0f): Pair<ScaleAnimation, ScaleAnimation> {
    val parent = (view.parent as View)
    val screenW = parent.width
    val screenH = parent.height
    val yBelowCenter = view.bottom - view.height / 2 - screenH / 2
    val xBelowCenter = view.right - view.width / 2 - screenW / 2
    val scaleX = screenW.toFloat() / view.width
    val scaleY = screenH.toFloat() / view.height
    val scaleFactor = min(scaleX, scaleY) * scaleRate
    val pivotY = 0.5f + yBelowCenter.toFloat() / (screenH / 2)
    val pivotX = 0.5f + xBelowCenter.toFloat() / (screenW / 2)
    val scaleUpAnimation = ScaleAnimation(
        1f, scaleFactor,
        1f, scaleFactor,
        Animation.RELATIVE_TO_SELF, pivotX,
        Animation.RELATIVE_TO_SELF, pivotY
    )
    val scaleDownAnimation = ScaleAnimation(
        scaleFactor, 1f,
        scaleFactor, 1f,
        Animation.RELATIVE_TO_SELF, pivotX,
        Animation.RELATIVE_TO_SELF, pivotY
    )
    return Pair(scaleUpAnimation, scaleDownAnimation)
}

fun getScreenSize(context: Context): Pair<Int, Int> {
    val displayMetrics = context.resources.displayMetrics
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
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

fun Context.dpToPx(dp: Float): Float {
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.pxToDp(px: Float): Float {
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}